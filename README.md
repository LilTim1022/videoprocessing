# Video processing
It's a video processing android app using Java and Python. It's a Python REST based client server app. Steps to run

For Api(REST server) :
1. For local get your local machine IP address and replace videoapi.py file's host part with this IP address (see line 173).
2. run videoapi.py file inside Api folder(Python) in Python server. 

For VideoApp(android client):
1. replace "apiBaseUrl " value with your Api's(Python) host and port (see MainActivity.java line 35)
2. build the android app in emulator or mobile from android studio
