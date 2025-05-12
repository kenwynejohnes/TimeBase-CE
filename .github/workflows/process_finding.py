import json, os

# Load the current finding and agent response
finding = json.load(open('current_finding.json'))['results'][0]
response = json.load(open('agent_response.json'))

# Extract the assistant's message from chat_history
assistant_message = next(msg['content'] for msg in response['chat_history'] if msg['role'] == 'assistant')

# Create link to the finding
link = f"{os.environ['REPO_URL']}/{finding['path']}#L{finding['start']['line']}"

# Prepare Slack message
summary = f"{assistant_message}\n\nLink: {link}"
payload = {
    'text': f"🔍 *Security Finding - {finding['check_id']}*\n```{summary}```"
}

# Save and send to Slack
with open('slack_payload.json', 'w') as f:
    json.dump(payload, f, ensure_ascii=False) 