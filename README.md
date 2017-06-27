# YOLO Example App
This app is an example of using TensorflowInferenceInterface to for object detection with YOLO. It is heavily based on <a href="https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android">Google's example app</a>, but has been trimmed to only include YOLO. See the full documentation on this project <a href="http://github.com/kerrj/yoloparser">here.</a>

The .pb (protobuf) file in <a href="/app/src/main/assets">app/src/main/assets</a> is used to store a Tensorflow neural net and currently contains a trained network which detects robots and red and blue wiffle balls.
