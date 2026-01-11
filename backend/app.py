from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
from bs4 import BeautifulSoup
from openai import OpenAI
import os
from dotenv import load_dotenv

# Load environment variables
load_dotenv()

# Initialize Flask app
app = Flask(__name__)
CORS(app)  # Allows React frontend to communicate with backend

# Set up OpenRouter API 
api_key = os.getenv('OPENAI_API_KEY')
if not api_key:
    print("Error: No OPENAI_API_KEY found in .env")
    client = None
else:
    client = OpenAI(
        api_key=api_key,
        base_url="https://openrouter.io/api/v1"
    )

# ROUTE 1: Health check (test if backend is running)
@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({"status": "Backend is running!"})

# ROUTE 2: Summarization endpoint
@app.route('/api/summarize', methods=['POST'])
def summarize():
    try:
        # Get URL from React frontend
        data = request.json
        url = data.get('url')
        
        if not url:
            return jsonify({"error": "No URL provided"}), 400
        
        print(f"Received URL: {url}")
        
        # STEP 1: Fetch the website content
        print("Fetching website...")
        website_text = fetch_website_text(url)
        
        if not website_text:
            return jsonify({"error": "Could not access website"}), 400
        
        # STEP 2: Use AI to summarize and extract key actions
        print("Summarizing....")
        summary, key_actions = summarize_with_ai(website_text)
        
        # STEP 3: Send response back to React
        return jsonify({
            "summary": summary,
            "keyActions": key_actions
        })
    
    except Exception as e:
        print(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500


# ROUTE 3: Extract actions endpoint
@app.route('/api/extract-actions', methods=['POST'])
def extract_actions():
    try:
        data = request.json
        url = data.get('url')
        if not url or not url.startswith(('http://', 'https://')):
            return jsonify({'error': 'Invalid URL'}), 400
        resp = requests.get(url, headers={
            # Prevents blocking by some websites that validate bots
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }, timeout=10)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.content, 'html.parser')
        actions = []
        # Extract <button>
        for btn in soup.find_all('button'):
            label = btn.get_text(strip=True)
            actions.append({
                'label': label,
                'url': url,
                'type': classify_type(label, url)
            })
        # Extract <a> with action keywords
        for a in soup.find_all('a', href=True):
            label = a.get_text(strip=True)
            href = a['href']
            if any(k in label.lower() for k in ['apply', 'submit', 'register', 'contact']):
                full_url = requests.compat.urljoin(url, href)
                actions.append({
                
                    'label': label,
                    'url': full_url,
                    'type': classify_type(label, href)
                })
        # Extract <input type="submit">
        for inp in soup.find_all('input', {'type': 'submit'}):
            label = inp.get('value', 'Submit')
            actions.append({
                'label': label,
                'url': url,
                'type': 'form_submit'
            })
        return jsonify(actions)
    except Exception as e:
        print(f"Error in extract_actions: {e}")
        return jsonify({'error': 'Failed to extract actions.'}), 500

# Determine common action types based on label
def classify_type(label, url):
    l = label.lower()
    if 'apply' in l:
        return 'job_application'
    if 'contact' in l:
        return 'contact'
    if 'register' in l:
        return 'register'
    if 'submit' in l:
        return 'form_submit'
    return 'other'


# FUNCTION 1: Fetch and clean website text
def fetch_website_text(url):
    """Get the text content from a website"""
    try:
        # Add https:// if not present
        if not url.startswith('http'):
            url = 'https://' + url
        
        # Fetch the website, prevent blocking by some websites
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        
        # Parse HTML to extract text
        soup = BeautifulSoup(response.content, 'html.parser')
        
        # Remove script and style elements
        for script in soup(["script", "style"]):
            script.decompose()
        
        # Get text
        text = soup.get_text()
        
        # Clean up whitespace
        lines = (line.strip() for line in text.splitlines())
        chunks = (phrase.strip() for line in lines for phrase in line.split("  "))
        text = ' '.join(chunk for chunk in chunks if chunk)
        
        # Limit to first 8000 characters 
        text = text[:8000]
        
        print(f"Successfully extracted {len(text)} characters from website")
        return text
    
    except Exception as e:
        print(f"Error fetching website: {str(e)}")
        return None


# FUNCTION 2: Use OpenRouter to summarize
def summarize_with_ai(website_text):
    """Use OpenRouter API to summarize and extract key actions"""
    try:
        if not client:
            return "No API key configured", ["Add API key to .env"]
        
        prompt = f"""You are helping someone with impaired vision navigate a website. 
        
Website content:
{website_text}

Please provide:
1. A SHORT summary (2-3 sentences max) of what this website is about and what the user can do here
2. A list of the 3-5 most important actions/buttons the user should know about (e.g., "Click Apply Button", "Fill Contact Form", etc.)

Focus on: 
- Clear simple language suitable for users with neurodivergent/ADHD conditions
- Only information that helps the user take action
- The purpose of the website


Ignore: 
- Visual layout. 
- Advertisements or information irrelevant to the website. Try to focus on information that relate to the actions/buttons the user should know. 
 
Format your response EXACTLY like this:
SUMMARY: [your 2-3 sentence summary here]
KEY_ACTIONS: [action 1]|[action 2]|[action 3]"""

        # Use only openai/gpt-3.5-turbo and the working endpoint
        api_key = os.getenv('OPENAI_API_KEY')
        endpoint = "https://openrouter.ai/api/v1/chat/completions"
        model = "openai/gpt-3.5-turbo"
        headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": "WebsiteSummaryTool/1.0"
        }
        payload = {
            "model": model,
            "messages": [{"role": "user", "content": prompt}],
            "temperature": 0.5,
            "max_tokens": 300
        }
        response = requests.post(endpoint, headers=headers, json=payload, timeout=30)
        print(f"   Response status: {response.status_code}")
        print(f"   Response headers: {dict(response.headers)}")
        print(f"   Response body (truncated): {response.text[:1000]}")
        if response.status_code != 200:
            return "AI service error", ["Please try again"]
        result = response.json()
        print(f"   Full OpenRouter JSON response: {result}")
        result_text = result.get('choices', [{}])[0].get('message', {}).get('content', '')
        
        # Parse the response
        lines = result_text.split('\n')
        summary = ""
        key_actions = []
        
        for line in lines:
            if line.startswith('SUMMARY:'):
                summary = line.replace('SUMMARY:', '').strip()
            elif line.startswith('KEY_ACTIONS:'):
                actions_str = line.replace('KEY_ACTIONS:', '').strip()
                key_actions = [action.strip() for action in actions_str.split('|') if action.strip()]
        
        print(f"AI Summary: {summary}")
        print(f"Key Actions: {key_actions}")
        
        return summary, key_actions
    
    except Exception as e:
        print(f"Error with AI: {str(e)}")
        return "Error summarizing website", ["Please try again"]


# Run the backend
if __name__ == '__main__':
    print("Backend starting on http://localhost:5000")
    print("Press CTRL+C to stop")
    app.run(debug=True, port=5000)