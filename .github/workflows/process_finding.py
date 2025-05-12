import json, os

# Load the current finding and agent response
finding = json.load(open('current_finding.json'))['results'][0]
response = json.load(open('agent_response.json'))
assistant = next(m['content'] for m in response['chat_history'] if m['role'] == 'assistant')

# Create link to the finding
link = f"{os.environ['REPO_URL']}/{finding['path']}#L{finding['start']['line']}"

# Prepare Slack message
summary = f"{assistant}\n\nLink: {link}"
payload = {
    'text': f"🔍 *Security Finding - {finding['check_id']}*\n```{summary}```"
}

# Save and send to Slack
with open('slack_payload.json', 'w') as f:
    json.dump(payload, f, ensure_ascii=False) 