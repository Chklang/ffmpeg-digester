# A simple java application to encode some pictures to a video

# Prerequires
- Java 1.8
- A ffmpeg built (like from [BtbN github repository](https://github.com/BtbN/FFmpeg-Builds/releases))

# How to use it ?
1. Create the folder on your computer which will contains all pictures
2. Ensure that this folder not have a file "stop.txt" (or the name given by the parameter --stop-file)
3. Start the program with correct parameters (you can use --help / -h to know them)

Use a command line like it :

```bash
java -jar ffmpeg-digester.jar --ffmpeg ffmpeg.exe --input ./pictures --output myVideo.mkv --codec libx264
```

# How to build it ?
mvn clean compile assembly:single
