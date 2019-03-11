package code;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import code.Recognizer.Results;
import javafx.scene.image.WritableImage;

public class ImageUtils {
	public static Mat bufferedImageToMat(BufferedImage bi) {
		  Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
		  byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
		  mat.put(0, 0, data);
		  return mat;
	}
	public static BufferedImage matToBufferedImage(Mat matrix)throws IOException {
	    MatOfByte mob=new MatOfByte();
	    Imgcodecs.imencode(".png", matrix, mob);
	    return ImageIO.read(new ByteArrayInputStream(mob.toArray()));
	}
	public static byte[] mat2Bytes(Mat mat) throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(matToBufferedImage(mat), "png", baos);
		return baos.toByteArray();
    }
	public static Mat addResults(Results res, AtomicReference<WritableImage> ref) {
		// TODO Auto-generated method stub
		return null;
	}

}
