package cs107;

import static cs107.Helper.Image;

/**
 * "Quite Ok Image" Decoder
 * @apiNote Third task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.0
 * @since 1.0
 */
public final class QOIDecoder {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private QOIDecoder(){}

    // ==================================================================================
    // =========================== QUITE OK IMAGE HEADER ================================
    // ==================================================================================

    /**
     * Extract useful information from the "Quite Ok Image" header
     * @param header (byte[]) - A "Quite Ok Image" header
     * @return (int[]) - Array such as its content is {width, height, channels, color space}
     * @throws AssertionError See handouts section 6.1
     */
    public static int[] decodeHeader(byte[] header){
        assert header != null: "Header is null";
        assert header.length ==QOISpecification.HEADER_SIZE;
        assert (ArrayUtils.equals(ArrayUtils.extract(header, 0, 4),QOISpecification.QOI_MAGIC)): "Header size not valid";
        assert (header[12] ==QOISpecification.RGB) ||(header[12] ==QOISpecification.RGBA) : "Canal channel number is invalid";
        assert (header[13] ==QOISpecification.ALL) ||(header[13] ==QOISpecification.sRGB) : "Color Space is invalid";

        byte[] height = ArrayUtils.extract(header,8, 4);
        int heightInt = ArrayUtils.toInt(height);
        byte[] width = ArrayUtils.extract(header,4, 4);
        int widthInt = ArrayUtils.toInt(width);
        int channelNumber = (int)(header[12]);
        int colorSpace= (int)(header[13]);
        int[] result = new int [] {widthInt, heightInt, channelNumber, colorSpace};
        return result;
    }

    // ==================================================================================
    // =========================== ATOMIC DECODING METHODS ==============================
    // ==================================================================================

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param alpha (byte) - Alpha component of the pixel
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.1
     */
    public static int decodeQoiOpRGB(byte[][] buffer, byte[] input, byte alpha, int position, int idx){
        assert buffer != null : "Buffer is null";
        assert input != null : "Input is null";
        assert position >= 0 && position < buffer.length: "Position out of bound";
        assert idx >= 0 && idx< input.length: "Idx out of bound";
        assert idx >= 0 && (idx+2)< input.length: "Idx out of bound";
        byte[] valueRGB = ArrayUtils.extract(input,idx, 3);
        byte[] valueRGBA = ArrayUtils.concat(valueRGB,ArrayUtils.wrap(alpha));
        buffer[position]= valueRGBA;
        return 3;
    }

    /**
     * Store the pixel in the buffer and return the number of consumed bytes
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param input (byte[]) - Stream of bytes to read from
     * @param position (int) - Index in the buffer
     * @param idx (int) - Index in the input
     * @return (int) - The number of consumed bytes
     * @throws AssertionError See handouts section 6.2.2
     */
    public static int decodeQoiOpRGBA(byte[][] buffer, byte[] input, int position, int idx){
        assert buffer != null : "Buffer is null";
        assert input != null : "Input is null";
        assert position >= 0 && position < buffer.length: "Position out of bound";
        assert idx >= 0 && idx< input.length: "Idx out of bound";
        assert idx >= 0 && (idx+3)< input.length: "Idx out of bound";
        byte[] valueRGBA = ArrayUtils.extract(input,idx, 4);
        buffer[position]= valueRGBA;
        return 4;
    }

    /**
     * Create a new pixel following the "QOI_OP_DIFF" schema.
     * @param previousPixel (byte[]) - The previous pixel
     * @param chunk (byte) - A "QOI_OP_DIFF" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.4
     */
    public static byte[] decodeQoiOpDiff(byte[] previousPixel, byte chunk){
        assert previousPixel != null: "PreviousPixel is null" ;
        assert previousPixel.length==4 : "Pixel invalid";
        assert (chunk & 0b11000000) == QOISpecification.QOI_OP_DIFF_TAG :"Tag invalid";
        byte maskR = (byte)(chunk & 0b00110000);
        byte maskG = (byte)(chunk & 0b00001100);
        byte maskB = (byte)(chunk & 0b00000011);
        byte dr = (byte)((maskR >> 4) - 2);
        byte dg = (byte)((maskG >> 2) - 2);
        byte db = (byte)(maskB - 2);
        byte[] valueRGBA = new byte []{(byte)(previousPixel[QOISpecification.r]+ dr),(byte)(previousPixel[QOISpecification.g]+ dg),(byte)(previousPixel[QOISpecification.b]+ db),previousPixel[QOISpecification.a]};
        return valueRGBA;
    }

    /**
     * Create a new pixel following the "QOI_OP_LUMA" schema
     * @param previousPixel (byte[]) - The previous pixel
     * @param data (byte[]) - A "QOI_OP_LUMA" data chunk
     * @return (byte[]) - The newly created pixel
     * @throws AssertionError See handouts section 6.2.5
     */
    public static byte[] decodeQoiOpLuma(byte[] previousPixel, byte[] data){
        assert previousPixel != null: "PreviousPixel is null" ;
        assert previousPixel.length == 4 : "Pixel invalid";
        assert (data[0] & 0b11000000) == QOISpecification.QOI_OP_LUMA_TAG:"Tag invalid";
        byte maskG = (byte)(data[0] & 0b00111111);
        byte dg = (byte)(maskG - 32);
        byte maskR_G = (byte)(data[1] & 0b11110000);
        byte dr = (byte)(((maskR_G >> 4) - 8) + dg);
        byte maskB_G = (byte)(data[1] & 0b00001111);
        byte db = (byte)((maskB_G + 8) + dg);
        byte[] valueRGBA = new byte []{(byte)(previousPixel[QOISpecification.r]+ dr),(byte)(previousPixel[QOISpecification.g]+ dg),(byte)(previousPixel[QOISpecification.b]+ db),previousPixel[QOISpecification.a]};
        return valueRGBA;
    }

    /**
     * Store the given pixel in the buffer multiple times
     * @param buffer (byte[][]) - Buffer where to store the pixel
     * @param pixel (byte[]) - The pixel to store
     * @param chunk (byte) - a QOI_OP_RUN data chunk
     * @param position (int) - Index in buffer to start writing from
     * @return (int) - number of written pixels in buffer
     * @throws AssertionError See handouts section 6.2.6
     */
    public static int decodeQoiOpRun(byte[][] buffer, byte[] pixel, byte chunk, int position){
        assert buffer != null : "Buffer is null";
        assert pixel.length==4 : "Pixel not valid";
        assert pixel != null :"Pixel is null";
        assert position>=0 && position< buffer.length : "Position out of bound";
        byte count = (byte)(chunk & 0b00111111);
        assert buffer[0].length == 4 && buffer.length >= count;
        for(int iBuffer=0; iBuffer <= count; ++iBuffer){
            buffer[position+iBuffer]= pixel;
        }
        return count;
    }

    // ==================================================================================
    // ========================= GLOBAL DECODING METHODS ================================
    // ==================================================================================

    /**
     * Decode the given data using the "Quite Ok Image" Protocol
     * @param data (byte[]) - Data to decode
     * @param width (int) - The width of the expected output
     * @param height (int) - The height of the expected output
     * @return (byte[][]) - Decoded "Quite Ok Image"
     * @throws AssertionError See handouts section 6.3
     */
    public static byte[][] decodeData(byte[] data, int width, int height){
        return Helper.fail("Not Implemented");
    }

    /**
     * Decode a file using the "Quite Ok Image" Protocol
     * @param content (byte[]) - Content of the file to decode
     * @return (Image) - Decoded image
     * @throws AssertionError if content is null
     */
    public static Image decodeQoiFile(byte[] content){
        return Helper.fail("Not Implemented");
    }

}