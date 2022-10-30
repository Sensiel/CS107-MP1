package cs107;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * "Quite Ok Image" Encoder
 * @apiNote Second task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.0
 * @since 1.0
 */
public final class QOIEncoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIEncoder(){}

    // ==================================================================================
    // ============================ QUITE OK IMAGE HEADER ===============================
    // ==================================================================================

    /**
     * Generate a "Quite Ok Image" header using the following parameters
     * @param image (Helper.Image) - Image to use
     * @throws AssertionError if the colorspace or the number of channels is corrupted or if the image is null.
     *  (See the "Quite Ok Image" Specification or the handouts of the project for more information)
     * @return (byte[]) - Corresponding "Quite Ok Image" Header
     */
    public static byte[] qoiHeader(Helper.Image image){
        assert image != null : "Image is null";
        int channelNumber = image.channels();
        assert channelNumber == QOISpecification.RGB || channelNumber == QOISpecification.RGBA : "Number of channels is corrupted";
        int colorSpace = image.color_space();
        assert colorSpace == QOISpecification.sRGB || colorSpace == QOISpecification.ALL : "Colorspace is corrupted";

        int[][] imageData = image.data();
        int height = imageData.length;
        int width = imageData[0].length;
        byte[] result =  ArrayUtils.concat(QOISpecification.QOI_MAGIC, ArrayUtils.fromInt(width), ArrayUtils.fromInt(height), ArrayUtils.wrap((byte)channelNumber), ArrayUtils.wrap((byte)colorSpace));
        return result;
    }

    // ==================================================================================
    // ============================ ATOMIC ENCODING METHODS =============================
    // ==================================================================================

    /**
     * Encode the given pixel using the QOI_OP_RGB schema
     * @param pixel (byte[]) - The Pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) - Encoding of the pixel using the QOI_OP_RGB schema
     */
    public static byte[] qoiOpRGB(byte[] pixel){
        assert pixel != null : "Pixel is null";
        assert pixel.length == 4 : "Pixel length is not 4";
        //System.out.println("RGB");
        return ArrayUtils.concat(QOISpecification.QOI_OP_RGB_TAG, pixel[0], pixel[1], pixel[2]);
    }

    /**
     * Encode the given pixel using the QOI_OP_RGBA schema
     * @param pixel (byte[]) - The pixel to encode
     * @throws AssertionError if the pixel's length is not 4
     * @return (byte[]) Encoding of the pixel using the QOI_OP_RGBA schema
     */
    public static byte[] qoiOpRGBA(byte[] pixel){
        assert pixel != null : "Pixel is null";
        assert pixel.length == 4 : "Pixel length is not 4";
        //System.out.println("RGBA");
        return ArrayUtils.concat(ArrayUtils.wrap(QOISpecification.QOI_OP_RGBA_TAG), pixel);
    }

    /**
     * Encode the index using the QOI_OP_INDEX schema
     * @param index (byte) - Index of the pixel
     * @throws AssertionError if the index is outside the range of all possible indices
     * @return (byte[]) - Encoding of the index using the QOI_OP_INDEX schema
     */
    public static byte[] qoiOpIndex(byte index){
        assert index >= 0 && index < 64 : "Index out of range";
        //System.out.println("Index");
        return ArrayUtils.wrap(index);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_DIFF schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpDiff(byte[] diff){
        assert diff != null : "Diff is null";
        assert diff.length == 3 : "Diff length is not 3";
        //System.out.println("Diff");
        byte result = 0;

        for(int iByte = 0; iByte < 3; iByte++){
            assert diff[iByte] > -3 && diff[iByte] < 2 : "diff out of range";
            result <<= 2;
            result |= diff[iByte] + 2;
        }

        result |= QOISpecification.QOI_OP_DIFF_TAG;
        return ArrayUtils.wrap(result);
    }

    /**
     * Encode the difference between 2 pixels using the QOI_OP_LUMA schema
     * @param diff (byte[]) - The difference between 2 pixels
     * @throws AssertionError if diff doesn't respect the constraints
     * or diff's length is not 3
     * (See the handout for the constraints)
     * @return (byte[]) - Encoding of the given difference
     */
    public static byte[] qoiOpLuma(byte[] diff){
        assert diff != null : "Diff is null";
        assert diff.length == 3 : "Diff length is not 3";
        byte[] result = new byte[2];
        assert diff[1] > -33 && diff[1] < 32 : "Diff out of bound";
        result[0] = (byte)((diff[1] + 32) | QOISpecification.QOI_OP_LUMA_TAG);
        byte diff1 = (byte)(diff[0]-diff[1]);
        byte diff2 = (byte)(diff[2]-diff[1]);
        assert diff1 > -9 && diff1 < 8 : "Diff1 out of range";
        assert diff2 > -9 && diff2 < 8 : "Diff2 out of range";
        diff1 += 8;
        diff2 += 8;
        result[1] = (byte)((diff1 << 4) | diff2);
        //System.out.println("Luma");
        return result;
    }

    /**
     * Encode the number of similar pixels using the QOI_OP_RUN schema
     * @param count (byte) - Number of similar pixels
     * @throws AssertionError if count is not between 0 (exclusive) and 63 (exclusive)
     * @return (byte[]) - Encoding of count
     */
    public static byte[] qoiOpRun(byte count){
        assert count >= 1 && count <= 62 : "Count is out of bound";
        //System.out.println("Run");
        return ArrayUtils.wrap((byte)((count - 1) | QOISpecification.QOI_OP_RUN_TAG));
    }

    // ==================================================================================
    // ============================== GLOBAL ENCODING METHODS  ==========================
    // ==================================================================================

    /**
     * Encode the given image using the "Quite Ok Image" Protocol
     * (See handout for more information about the "Quite Ok Image" protocol)
     * @param image (byte[][]) - Formatted image to encode
     * @return (byte[]) - "Quite Ok Image" representation of the image
     */
    public static byte[] encodeData(byte[][] image){
        assert image != null : "Image is null";
        byte[] ancien = QOISpecification.START_PIXEL;
        byte[][] hashTable = new byte[64][4];
        int compteur = 0;
        ArrayList<byte[]> tab = new ArrayList<>();
        for(int iPixel = 0; iPixel < image.length; iPixel++) {
            assert image[iPixel] != null : "Current pixel is null";
            assert image[iPixel].length == 4 : "Current pixel is invalid";
            if(iPixel != 0){
                ancien = image[iPixel - 1];
            }
            if (ArrayUtils.equals(ancien, image[iPixel])) {
                compteur++;
                if (compteur == 62 || iPixel == image.length - 1) {
                    tab.add(qoiOpRun((byte) compteur));
                    compteur = 0;
                }
                continue;
            } else if (compteur != 0) {
                tab.add(qoiOpRun((byte) compteur));
                compteur = 0;
            }

            if (ArrayUtils.equals(hashTable[QOISpecification.hash(image[iPixel])], image[iPixel])) {
                tab.add(qoiOpIndex(QOISpecification.hash(image[iPixel])));
                continue;
            } else {
                hashTable[QOISpecification.hash(image[iPixel])] = image[iPixel];
            }

            if (image[iPixel][QOISpecification.a] == ancien[QOISpecification.a]) {
                byte dr = (byte)(image[iPixel][QOISpecification.r] - ancien[QOISpecification.r]);
                byte dg = (byte)(image[iPixel][QOISpecification.g] - ancien[QOISpecification.g]);
                byte db = (byte)(image[iPixel][QOISpecification.b] - ancien[QOISpecification.b]);
                if ((dr > -3 && dr < 2) && (dg > -3 && dg < 2) && (db > -3 && db < 2)) {
                    byte[] diff = new byte[]{dr, dg, db};
                    tab.add(qoiOpDiff(diff));
                    continue;
                }
                byte dRG = (byte)(dr - dg);
                byte dBG = (byte)(db - dg);
                if((dg > -33 && dg < 32) && (dRG > -9 && dRG < 8) && (dBG > -9 && dBG < 8)){
                    byte[] luma = new byte[]{dr, dg, db};
                    tab.add(qoiOpLuma(luma));
                    continue;
                }

                tab.add(qoiOpRGB(image[iPixel]));
                continue;
            }
            tab.add(qoiOpRGBA(image[iPixel]));
        }
        byte[][] result = new byte[tab.size()][];
        byte[] temp = ArrayUtils.concat(tab.toArray(result));
        //Hexdump.hexdump(temp);
        return ArrayUtils.concat(tab.toArray(result));
    }

    /**
     * Creates the representation in memory of the "Quite Ok Image" file.
     * @apiNote THE FILE IS NOT CREATED YET, THIS IS JUST ITS REPRESENTATION.
     * TO CREATE THE FILE, YOU'LL NEED TO CALL Helper::write
     * @param image (Helper.Image) - Image to encode
     * @return (byte[]) - Binary representation of the "Quite Ok File" of the image
     * @throws AssertionError if the image is null
     */
    public static byte[] qoiFile(Helper.Image image){
        assert image != null : "Image is null";
        byte[] header = qoiHeader(image);
        byte[] signature = QOISpecification.QOI_EOF;
        byte[] body = encodeData(ArrayUtils.imageToChannels(image.data()));
        return ArrayUtils.concat(header, body, signature);
    }

}