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

4. When process is done juste create a file "stop.txt" (or the name given by the parameter --stop-file) into the input folder, and the application will give the last frame to ffmpeg and stop it.

# How to build it ?
mvn clean compile assembly:single

# Some questions
1. I've created one picture and the program don't give it to ffmpeg
The program will attempt that the picture "n+1" will exists to digest "n". The reason is to ensure that the "n" is finished to be wrotte on disk before try to read it.
