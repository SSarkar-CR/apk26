#!/usr/bin/env python3
import markdown
from weasyprint import HTML, CSS

# Read markdown file
with open('/home/user/apk26/CODE_REVIEW_REPORT.md', 'r') as f:
    md_content = f.read()

# Convert to HTML
html_content = markdown.markdown(md_content, extensions=['tables', 'fenced_code'])

# Create styled HTML document
styled_html = f'''
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Code Review Report</title>
    <style>
        @page {{
            size: A4;
            margin: 2cm;
        }}
        body {{
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
            font-size: 11pt;
            line-height: 1.6;
            color: #333;
        }}
        h1 {{
            color: #1a5276;
            border-bottom: 3px solid #1a5276;
            padding-bottom: 10px;
            font-size: 24pt;
        }}
        h2 {{
            color: #2874a6;
            border-bottom: 2px solid #2874a6;
            padding-bottom: 5px;
            margin-top: 30px;
            font-size: 18pt;
        }}
        h3 {{
            color: #2e86c1;
            margin-top: 25px;
            font-size: 14pt;
        }}
        h4 {{
            color: #3498db;
            margin-top: 20px;
            font-size: 12pt;
        }}
        table {{
            border-collapse: collapse;
            width: 100%;
            margin: 15px 0;
            font-size: 10pt;
        }}
        th, td {{
            border: 1px solid #bdc3c7;
            padding: 8px 12px;
            text-align: left;
        }}
        th {{
            background-color: #2874a6;
            color: white;
            font-weight: bold;
        }}
        tr:nth-child(even) {{
            background-color: #f2f3f4;
        }}
        tr:hover {{
            background-color: #d5dbdb;
        }}
        code {{
            background-color: #f4f4f4;
            padding: 2px 6px;
            border-radius: 3px;
            font-family: 'Consolas', 'Courier New', monospace;
            font-size: 9pt;
        }}
        pre {{
            background-color: #2c3e50;
            color: #ecf0f1;
            padding: 15px;
            border-radius: 5px;
            overflow-x: auto;
            font-size: 9pt;
            line-height: 1.4;
        }}
        pre code {{
            background-color: transparent;
            color: #ecf0f1;
            padding: 0;
        }}
        blockquote {{
            border-left: 4px solid #3498db;
            margin: 15px 0;
            padding: 10px 20px;
            background-color: #ebf5fb;
        }}
        hr {{
            border: none;
            border-top: 2px solid #bdc3c7;
            margin: 30px 0;
        }}
        strong {{
            color: #1a5276;
        }}
        .critical {{
            color: #e74c3c;
            font-weight: bold;
        }}
        .moderate {{
            color: #f39c12;
            font-weight: bold;
        }}
        .good {{
            color: #27ae60;
            font-weight: bold;
        }}
        ul, ol {{
            margin: 10px 0;
            padding-left: 25px;
        }}
        li {{
            margin: 5px 0;
        }}
    </style>
</head>
<body>
{html_content}
</body>
</html>
'''

# Generate PDF
HTML(string=styled_html).write_pdf('/home/user/apk26/CODE_REVIEW_REPORT.pdf')

print("PDF generated successfully: /home/user/apk26/CODE_REVIEW_REPORT.pdf")
