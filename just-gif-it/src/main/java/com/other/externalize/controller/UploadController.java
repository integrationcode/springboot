package com.other.externalize.controller;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.justgifit.services.ConverterService;
import com.justgifit.services.GifEncoderService;
import com.justgifit.services.VideoDecoderService;
import com.madgag.gif.fmsware.AnimatedGifEncoder;

@RestController
public class UploadController {

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
	

	@Value("${spring.servlet.multipart.location}")
	private String location;

	@Inject
	private ConverterService converterService;

	@Inject
	private GifEncoderService gifEncoderService;

	@Inject
	private VideoDecoderService videoDecoderService;

	@PostMapping(path = "/upload", produces = MediaType.IMAGE_GIF_VALUE)
	public String upload(@RequestPart("file") MultipartFile file, @RequestParam("start") int start,
			@RequestParam("end") int end, @RequestParam("speed") int speed, @RequestParam("repeat") boolean repeat)
			throws IOException, FrameGrabber.Exception {
		File videoFile = new File(location + "/" + System.currentTimeMillis() + ".mp4");
		file.transferTo(videoFile);

		log.info("Saved video file to {}", videoFile.getAbsolutePath());

		Path output = Paths.get(location + "/gif/" + System.currentTimeMillis() + ".gif");

		FFmpegFrameGrabber frameGrabber = videoDecoderService.read(videoFile);
		AnimatedGifEncoder gifEncoder = gifEncoderService.getGifEncoder(repeat, (float) frameGrabber.getFrameRate(),
				output);
		converterService.toAnimatedGif(frameGrabber, gifEncoder, start, end, speed);

		log.info("Saved generated gif to {}", output.toString());

		return output.getFileName().toString();
	}

}
