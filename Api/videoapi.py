import re
from flask import Flask, request, Response
from flask_restful import Resource, Api
from pathlib import Path
import cv2
import os
from os.path import isfile, join

app = Flask(__name__)
api = Api(app)


def get_video_byte(file_path, byte1=None, byte2=None):
    file_size = os.stat(file_path).st_size
    start = 0
    if byte1 < file_size:
        start = byte1
    if byte2:
        length = byte2 + 1 - byte1
    else:
        length = file_size - start
    with open(file_path, 'rb') as file:
        file.seek(start)
        video_byte = file.read(length)
    return video_byte, start, length, file_size


def video_response(file_path):
    header_range = request.headers.get('Range', None)
    byte1, byte2 = 0, None
    if header_range:
        match = re.search(r'(\d+)-(\d*)', header_range)
        groups = match.groups()
        if groups[0]:
            byte1 = int(groups[0])
        if groups[1]:
            byte2 = int(groups[1])
    video_byte, start, length, file_size = get_video_byte(file_path, byte1, byte2)
    resp = Response(video_byte, 206, mimetype='video/mp4',
                    content_type='video/mp4; charset=utf-8', direct_passthrough=True)
    resp.headers.add('Content-Range', 'bytes {0}-{1}/{2}'.format(start, start + length - 1, file_size))
    return resp


def frames_to_video(input_path, output_path, fps):
    image_array = []
    files = [f for f in os.listdir(input_path) if isfile(join(input_path, f))]
    files.sort(key=lambda x: int(x[5:-4]))
    for i in range(len(files)):
        img = cv2.imread(input_path + files[i])
        sizes = (img.shape[1], img.shape[0])
        img = cv2.resize(img, sizes)
        image_array.append(img)
    out = cv2.VideoWriter(output_path, cv2.VideoWriter_fourcc(*'DIVX'), fps, sizes)
    for i in range(len(image_array)):
        out.write(image_array[i])
    out.release()


class GetVideo(Resource):
    def get(self):
        resp = video_response("videos/sample2.mp4")
        return resp


class GetOverlayVideo(Resource):
    def get(self):
        resp = video_response("videos/sample3.mp4")
        return resp


class InsertOverlay(Resource):
    def post(self):
        # read displayed video
        cap = cv2.VideoCapture('videos/sample2.mp4')
        fps = cap.get(cv2.CAP_PROP_FPS)

        # delete existing converted video
        if os.path.exists("videos/sample3.mp4"):
            os.remove("videos/sample3.mp4")
        count = 1
        x_position = 160
        y_position = 130
        while True:
            # Capture frames in the video
            has_frame, frame = cap.read()
            # describe the type of font to be used.
            font = cv2.FONT_HERSHEY_SIMPLEX

            # inserting a dot on each frame
            cv2.putText(frame,
                        '.',
                        (x_position, y_position),
                        font, 1,
                        (0, 255, 255),
                        2,
                        cv2.LINE_4)

            # Save the each resulting frame with dot in local 'images' folder
            if not has_frame:
                break
            Path("images").mkdir(parents=True, exist_ok=True)
            cv2.imwrite("images/frame" + str(count) + ".jpg", frame)  # Save frame as JPG file
            count = count + 1

        # release the cap object
        cap.release()

        # convert frames with dot to video and display to the user
        frames_to_video("images/", "videos/sample3.mp4", fps)
        return {'overlayUrl': 'getOverlayVideo'}


class UpdateOverlay(Resource):
    def put(self, x_value, y_value):
        # read displayed video
        cap = cv2.VideoCapture('videos/sample2.mp4')
        fps = cap.get(cv2.CAP_PROP_FPS)

        # delete existing converted video
        if os.path.exists("videos/sample3.mp4"):
            os.remove("videos/sample3.mp4")
        count = 1
        x_position = 160
        y_position = 130
        if x_value:
            x_position = x_value + x_position

        if y_value:
            y_position = y_value + y_position
        print(x_position)
        print(y_position)
        while True:
            # Capture frames in the video
            has_frame, frame = cap.read()
            # describe the type of font to be used.
            font = cv2.FONT_HERSHEY_SIMPLEX

            # inserting a dot on each frame
            cv2.putText(frame,
                        '.',
                        (int(x_position), int(y_position)),
                        font, 1,
                        (0, 255, 255),
                        2,
                        cv2.LINE_4)

            # Save the each resulting frame with dot in local 'images' folder
            if not has_frame:
                break
            Path("images").mkdir(parents=True, exist_ok=True)
            cv2.imwrite("images/frame" + str(count) + ".jpg", frame)  # Save frame as JPG file
            count = count + 1

        # release the cap object
        cap.release()

        # convert frames with dot to video and display to the user
        frames_to_video("images/", "videos/sample3.mp4", fps)
        return {'overlayUrl': 'getOverlayVideo'}


# Route 1 to get video
api.add_resource(GetVideo, '/getVideo')
# Route 2 to insert overlay
api.add_resource(InsertOverlay, '/insertOverlay')
# Route 3 to move overlay
api.add_resource(UpdateOverlay, '/updateOverlay/<float(signed=True):x_value>/<float(signed=True):y_value>')
# Route 4 to get overlayed video
api.add_resource(GetOverlayVideo, '/getOverlayVideo')

if __name__ == '__main__':
    app.run(host='192.168.2.1', port='5002')  # REST API server host and port where run
