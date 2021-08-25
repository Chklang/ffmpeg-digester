package chklang.ffmpegdigester.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class Application {
	private static String FFMPEG_BIN = "C:\\Users\\Shadow\\Downloads\\ffmpeg\\bin\\ffmpeg.exe";

	public static void main(String[] args) throws IOException {
		String input = "pctures";
		String codec = "libx265";
		String output = "my_output_videofile.mkv";
		String prefixPictures = "";
		String suffixePictures = ".jpg";
		int numberLength = 6;
		int framerate = 24;
		String stopFileName = "stop.txt";
		boolean disableFmmpegOutput = false;
		boolean disableLogsOutput = false;
		boolean disableClean = false;
		int startNumber = 0;
		int stepValue = 1;
		for (int i = 0; i < args.length; i++) {
			if ("--ffmpeg".equals(args[i])) {
				Application.FFMPEG_BIN = args[i + 1];
			} else if ("--input".equals(args[i])) {
				input = args[i + 1];
			} else if ("--codec".equals(args[i])) {
				codec = args[i + 1];
			} else if ("--output".equals(args[i])) {
				output = args[i + 1];
			} else if ("--prefix-pictures".equals(args[i])) {
				prefixPictures = args[i + 1];
			} else if ("--suffixe-pictures".equals(args[i])) {
				suffixePictures = args[i + 1];
			} else if ("--number-length".equals(args[i])) {
				numberLength = Integer.parseInt(args[i + 1]);
			} else if ("--framerate".equals(args[i])) {
				framerate = Integer.parseInt(args[i + 1]);
			} else if ("--stop-file".equals(args[i])) {
				stopFileName = args[i + 1];
			} else if ("--disable-ffmmpeg-output".equals(args[i])) {
				disableFmmpegOutput = "true".equals(args[i + 1]);
			} else if ("--disable-logs-output".equals(args[i])) {
				disableLogsOutput = "true".equals(args[i + 1]);
			} else if ("--disable-clean".equals(args[i])) {
				disableClean = "true".equals(args[i + 1]);
			} else if ("--start-number".equals(args[i])) {
				startNumber = Integer.parseInt(args[i + 1]);
			} else if ("--step-value".equals(args[i])) {
				stepValue = Integer.parseInt(args[i + 1]);
			} else if ("--help".equals(args[i]) || "-h".equals(args[i])) {
				Application.showHelp();
				System.exit(0);
			}
		}

		String[] command = new String[] { FFMPEG_BIN, // Bin path
				"-y", // (optional) overwrite output file if it exists
				"-f", "image2pipe", // Video type
				"-r", Integer.toString(framerate), // frames per second
				"-i", "-", // The imput comes from a pipe
				"-an", // Tells FFMPEG not to expect any audio
				"-c:v", codec, output// Output file
		};
		File files = new File(input);
		File stopFile = new File(files, stopFileName);

		Process process = Runtime.getRuntime().exec(command);
		final OutputStream stdin = process.getOutputStream();
		final InputStream stderr = process.getErrorStream();
		final InputStream stdout = process.getInputStream();

		final String prefixPicturesFinal = prefixPictures;
		final String suffixePicturesFinal = suffixePictures;
		final int numberLengthFinal = numberLength;
		final boolean disableFmmpegOutputFinal = disableFmmpegOutput;
		final boolean disableLogsOutputFinal = disableLogsOutput;
		final boolean disableCleanFinal = disableClean;
		final int startNumberFinal = startNumber;
		final int stepValueFinal = stepValue;

		new Thread(() -> {
			try {
				byte[] buffer = new byte[1024];
				int nbRead = 0;
				while (nbRead >= 0) {
					nbRead = stdout.read(buffer, 0, 1024);
					if (nbRead >= 0 && !disableFmmpegOutputFinal) {
						System.out.write(buffer, 0, nbRead);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}).start();

		new Thread(() -> {
			byte[] buffer = new byte[1024];
			int nbRead = 0;
			while (nbRead >= 0) {
				try {
					nbRead = stderr.read(buffer, 0, 1024);
				} catch (IOException e) {
					e.printStackTrace();
					nbRead = -1;
				}
				if (nbRead >= 0) {
					System.err.write(buffer, 0, nbRead);
				}
			}
		}).start();

		new Thread(() -> {
			int currentIndex = startNumberFinal;
			List<File> toDelete = new ArrayList<>();
			List<File> toDeleteOk = new ArrayList<>();
			byte[] buffer = new byte[1024];
			while (true) {
				try {
					String filename = Application.formatName(currentIndex, prefixPicturesFinal, suffixePicturesFinal, numberLengthFinal);
					File filePath = new File(files, filename);
					while (!filePath.exists()) {
						if (stopFile.exists()) {
							break;
						}
						Thread.sleep(1000);
					}
					// Here current file exists (or stop file detected)
					// Wait the next picture
					String filenameNext = Application.formatName(currentIndex + 1, prefixPicturesFinal, suffixePicturesFinal, numberLengthFinal);
					File filePathNext = new File(files, filenameNext);
					while (!filePathNext.exists()) {
						if (stopFile.exists()) {
							break;
						}
						Thread.sleep(1000);
					}
					// Here next file exists (or stop file detected)
					// So if current file exists, digest it!
					if (filePath.exists()) {
						if (!disableLogsOutputFinal) {
							System.out.println("Write " + filename);
						}
						FileInputStream picture = new FileInputStream(filePath);
						int nbRead = 0;
						while (nbRead >= 0) {
							nbRead = picture.read(buffer, 0, 1024);
							if (nbRead >= 0) {
								stdin.write(buffer, 0, nbRead);
							}
						}
						picture.close();
						if (!disableCleanFinal) {
							toDeleteOk.clear();
							toDelete.forEach((path) -> {
								if (path.delete()) {
									toDeleteOk.add(path);
								}
							});
							toDeleteOk.forEach((path) -> toDelete.remove(path));
							if (!filePath.delete()) {
								toDelete.add(filePath);
							}
						}
					}
					if (stopFile.exists()) {
						stdin.close();
						return;
					}
					currentIndex += stepValueFinal;
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}).start();
	}

	private static void showHelp() {
		System.out.println("FFmpeg digester");
		System.out.println("Commands :");
		System.out.println("\t--ffmpeg					[system path]		Path to ffmpeg (default : ffmpeg.exe)");
		System.out.println("\t--input					[system path]		Path to pictures (folder parent) (default : pictures)");
		System.out.println("\t--output					[system path]		Path to the created video (default : my_output_videofile.mkv)");
		System.out.println("\t--codec					[String]			Codec to use for encoding (default : libx265)");
		System.out.println("\t--prefix-pictures			[String]			Prefixe for pictures names (default : <empty>)");
		System.out.println("\t--suffixe-pictures		[String]			Suffixe for pictures names (default : .jpg)");
		System.out.println("\t--number-length			[Integer positive]	Length of the integer of picture number (default: 6)");
		System.out.println("\t--start-number			[Integer positive]	Number of first picture (default: 0)");
		System.out.println("\t--step-value				[Integer positive]	Step between each picture number (default: 1)");
		System.out.println("\t--framerate				[Integer]			Framerate (default : 24)");
		System.out.println("\t--ffmpeg					[system path]		Path to ffmpeg (default : ffmpeg.exe)");
		System.out.println("\t--stop-file				[String]			Stop file name (default : stop.txt)");
		System.out.println("\t--disable-ffmmpeg-output	[Boolean]			Disable redirection of ffmpeg stdout to this program stdout (not disable stderr) (default : false)");
		System.out.println("\t--disable-logs-output		[Boolean]			Disable logs of this program (\"Write picture00023.jpg\") (default : false)");
		System.out.println("\t--disable-clean			[Boolean]			Disable deletion of pictures after read (default : false)");
		System.out.println("\t--help					[No value]			Show this help");
	}

	private static String formatName(int index, String prefix, String suffixe, int numberLength) {
		String result = Integer.toString(index);
		while (result.length() < numberLength) {
			result = "0" + result;
		}
		return prefix + result + suffixe;
	}
}
