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
CORS(app)  # Allows React frontend to communicate with this backend

# Set up OpenRouter API (using OpenAI-compatible client)
api_key = os.getenv('OPENAI_API_KEY')
if not api_key:
    print("❌ CRITICAL: No OPENAI_API_KEY found in .env file!")
    print("   Create a .env file with: OPENAI_API_KEY=your_openrouter_key_here")
    client = None
else:
    print(f"✅ API Key loaded (length: {len(api_key)} characters)")
    # Initialize OpenAI client configured for OpenRouter
    client = OpenAI(
        api_key=api_key,
        base_url="https://openrouter.io/api/v1"
    )

# ROUTE 1: Health check (test if backend is running)
@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({"status": "Backend is running!"})

# ROUTE 2: Main summarization endpoint
@app.route('/api/summarize', methods=['POST'])
def summarize():
    try:
        # Get URL from React frontend
        data = request.json
        url = data.get('url')
        
        print("\n" + "="*60)
        print("NEW REQUEST RECEIVED")
        print("="*60)
        
        if not url:
            error_msg = "No URL provided in request"
            print(f"❌ {error_msg}")
            return jsonify({"error": error_msg}), 400
        
        print(f"📥 Received URL: {url}")
        
        # STEP 1: Fetch the website content
        print("🌐 Fetching website...")
        website_text = fetch_website_text(url)
        
        if not website_text:
            error_msg = "Could not extract text from website. Website might be blocking scrapers or URL is invalid."
            print(f"❌ {error_msg}")
            return jsonify({"error": error_msg}), 400
        
        # STEP 2: Use AI to summarize and extract key actions
        print("🤖 Using AI to summarize...")
        summary, key_actions = summarize_with_ai(website_text)
        
        # STEP 3: Send response back to React
        print("✅ SUCCESS! Sending response to frontend...")
        print("="*60 + "\n")
        return jsonify({
            "summary": summary,
            "keyActions": key_actions
        })
    
    except Exception as e:
        print(f"❌ EXCEPTION ERROR: {type(e).__name__}")
        print(f"❌ Error details: {str(e)}")
        print("="*60 + "\n")
        return jsonify({"error": f"Error: {str(e)}"}), 500


# FUNCTION 1: Fetch and clean website text
def fetch_website_text(url):
    """Get the text content from a website"""
    try:
        # Add https:// if not present
        if not url.startswith('http'):
            url = 'https://' + url
        
        print(f"   Full URL: {url}")
        
        # Fetch the website
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        
        print("   Sending request to website...")
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        
        print(f"   Status code: {response.status_code}")
        
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
        
        # Limit to first 4000 characters to stay within AI token limits
        text = text[:4000]
        
        print(f"✅ Extracted {len(text)} characters from website")
        return text
    
    except requests.exceptions.Timeout:
        print(f"❌ Website took too long to respond (timeout)")
        return None
    except requests.exceptions.ConnectionError:
        print(f"❌ Could not connect to website - check URL or internet connection")
        return None
    except requests.exceptions.HTTPError as e:
        print(f"❌ Website returned error: {e.response.status_code}")
        return None
    except Exception as e:
        print(f"❌ Error fetching website: {type(e).__name__}: {str(e)}")
        return None


# FUNCTION 2: Use OpenRouter to summarize
def summarize_with_ai(website_text):
    """Use OpenRouter API (with OpenAI-compatible client) to summarize"""
    try:
        if not client:
            print("❌ No API key configured!")
            return "No API key", ["Configure your API key in .env"]
        
        print("   Sending request to OpenRouter API...")
        
        prompt = f"""You are helping someone with impaired vision navigate a website. 
        
Website content:
{website_text}

Please provide:
1. A SHORT summary (2-3 sentences max) of what this website is about and what the user can do here
2. A list of the 3-5 most important actions/buttons the user should know about (e.g., "Click Apply Button", "Fill Contact Form", etc.)

Format your response EXACTLY like this:
SUMMARY: [your 2-3 sentence summary here]
KEY_ACTIONS: [action 1]|[action 2]|[action 3]"""

        response = client.chat.completions.create(
            model="openrouter/auto",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.5,
            max_tokens=300
        )
        
        result_text = response.choices[0].message.content
        print(f"   Raw AI response: {result_text[:100]}...")
        
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
        
        print(f"✅ AI Summary: {summary[:50]}...")
        print(f"✅ Key Actions found: {len(key_actions)}")
        
        return summary, key_actions
    
    except Exception as e:
        print(f"❌ Error with AI: {type(e).__name__}: {str(e)}")
        return "Error summarizing website", ["Please try again"]


# Run the backend
if __name__ == '__main__':
    print("\n" + "="*60)
    print("🚀 STARTING BACKEND SERVER")
    print("="*60)
    print(f"API Key configured: {bool(client)}")
    print("Using: OpenRouter API (via OpenAI-compatible client)")
    print("Listening on http://localhost:5000")
    print("Press CTRL+C to stop\n")
    app.run(debug=True, port=5000)

# ROUTE 1: Health check (test if backend is running)
@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({"status": "Backend is running!"})

# ROUTE 2: Main summarization endpoint
@app.route('/api/summarize', methods=['POST'])
def summarize():
    try:
        # Get URL from React frontend
        data = request.json
        url = data.get('url')
        
        print("\n" + "="*60)
        print("NEW REQUEST RECEIVED")
        print("="*60)
        
        if not url:
            error_msg = "No URL provided in request"
            print(f"❌ {error_msg}")
            return jsonify({"error": error_msg}), 400
        
        print(f"📥 Received URL: {url}")
        
        # STEP 1: Fetch the website content
        print("🌐 Fetching website...")
        website_text = fetch_website_text(url)
        
        if not website_text:
            error_msg = "Could not extract text from website. Website might be blocking scrapers or URL is invalid."
            print(f"❌ {error_msg}")
            return jsonify({"error": error_msg}), 400
        
        # STEP 2: Use AI to summarize and extract key actions
        print("🤖 Using AI to summarize...")
        summary, key_actions = summarize_with_ai(website_text)
        
        # STEP 3: Send response back to React
        print("✅ SUCCESS! Sending response to frontend...")
        print("="*60 + "\n")
        return jsonify({
            "summary": summary,
            "keyActions": key_actions
        })
    
    except Exception as e:
        print(f"❌ EXCEPTION ERROR: {type(e).__name__}")
        print(f"❌ Error details: {str(e)}")
        print("="*60 + "\n")
        return jsonify({"error": f"Error: {str(e)}"}), 500


# FUNCTION 1: Fetch and clean website text
def fetch_website_text(url):
    """Get the text content from a website"""
    try:
        # Add https:// if not present
        if not url.startswith('http'):
            url = 'https://' + url
        
        print(f"   Full URL: {url}")
        
        # Fetch the website
        headers = {
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36'
        }
        
        print("   Sending request to website...")
        response = requests.get(url, headers=headers, timeout=10)
        response.raise_for_status()
        
        print(f"   Status code: {response.status_code}")
        
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
        
        # Limit to first 4000 characters to stay within AI token limits
        text = text[:4000]
        
        print(f"✅ Extracted {len(text)} characters from website")
        return text
    
    except requests.exceptions.Timeout:
        print(f"❌ Website took too long to respond (timeout)")
        return None
    except requests.exceptions.ConnectionError:
        print(f"❌ Could not connect to website - check URL or internet connection")
        return None
    except requests.exceptions.HTTPError as e:
        print(f"❌ Website returned error: {e.response.status_code}")
        return None
    except Exception as e:
        print(f"❌ Error fetching website: {type(e).__name__}: {str(e)}")
        return None


# FUNCTION 2: Use OpenAI to summarize
def summarize_with_ai(website_text):
    """Use ChatGPT to summarize and extract key actions"""
    try:
        if not openai.api_key:
            print("❌ No API key configured!")
            return "No API key", ["Configure your API key in .env"]
        
        print("   Sending request to OpenAI API...")
        
        prompt = f"""You are helping someone with impaired vision navigate a website. 
        
Website content:
{website_text}

Please provide:
1. A SHORT summary (2-3 sentences max) of what this website is about and what the user can do here
2. A list of the 3-5 most important actions/buttons the user should know about (e.g., "Click Apply Button", "Fill Contact Form", etc.)

Format your response EXACTLY like this:
SUMMARY: [your 2-3 sentence summary here]
KEY_ACTIONS: [action 1]|[action 2]|[action 3]"""

        response = client.chat.completions.create(
            model="openrouter/auto",
            messages=[{"role": "user", "content": prompt}],
            temperature=0.5,
            max_tokens=300
        )
        
        result_text = response.choices[0].message.content
        print(f"   Raw AI response: {result_text[:100]}...")
        
        # Parse the response
        lines = result_text.split('\n')
        summary = ""
        key_actions = []
        
        for line in lines:
            if line.startswith('SUMMARY:'):
                summary = line.replace('SUMMARY:', '').strip()
            elif line.startswith('KEY_ACTIONS:'):
                actions_str = line.replace('KEY_ACTIONS:', '').strip()
                key_actions = [action.strip() for action in actions_str.split('|')]
        
        print(f"✅ AI Summary: {summary[:50]}...")
        print(f"✅ Key Actions found: {len(key_actions)}")
        
        return summary, key_actions
    
    except openai.error.AuthenticationError:
        print(f"❌ OpenAI Authentication Error: Check your API key in .env")
        return "Authentication error", ["Check your API key"]
    except openai.error.RateLimitError:
        print(f"❌ OpenAI Rate Limit: Too many requests, wait a moment and try again")
        return "Rate limit exceeded", ["Try again in a moment"]
    except openai.error.APIError as e:
        print(f"❌ OpenAI API Error: {str(e)}")
        return "API Error", ["OpenAI service issue"]
    except Exception as e:
        print(f"❌ Error with AI: {type(e).__name__}: {str(e)}")
        return "Error summarizing website", ["Please try again"]


# Run the backend
if __name__ == '__main__':
    print("\n" + "="*60)
    print("🚀 STARTING BACKEND SERVER")
    print("="*60)
    print(f"API Key configured: {bool(openai.api_key)}")
    print("Listening on http://localhost:5000")
    print("Press CTRL+C to stop\n")
    app.run(debug=True, port=5000)
