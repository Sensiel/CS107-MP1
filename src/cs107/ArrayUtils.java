package cs107;

/**
 * Utility class to manipulate arrays.
 * @apiNote First Task of the 2022 Mini Project
 * @author Hamza REMMAL (hamza.remmal@epfl.ch)
 * @version 1.0
 * @since 1.0
 */
public final class ArrayUtils {

    /**
     * DO NOT CHANGE THIS, MORE ON THAT IN WEEK 7.
     */
    private ArrayUtils(){}

    // ==================================================================================
    // =========================== ARRAY EQUALITY METHODS ===============================
    // ==================================================================================

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[]) - First array
     * @param a2 (byte[]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[] a1, byte[] a2){
        assert (a1 == null) == (a2 == null) : "Both array are null";
        if(a1 == null)
            return true;
        if(a1.length != a2.length)
            return false;
        for(int iByte = 0; iByte < a1.length; iByte++){
            if(a1[iByte] != a2[iByte]){
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the content of both arrays is the same
     * @param a1 (byte[][]) - First array
     * @param a2 (byte[][]) - Second array
     * @return (boolean) - true if both arrays have the same content (or both null), false otherwise
     * @throws AssertionError if one of the parameters is null
     */
    public static boolean equals(byte[][] a1, byte[][] a2){
        assert (a1 == null) == (a2 == null) : "Only one of the array is null";
        if(a1 == null)
            return true;
        if(a1.length != a2.length)
            return false;
        for(int iArray = 0; iArray < a1.length; iArray++){
            if(!equals(a1[iArray], a2[iArray])){
                return false;
            }
        }
        return true;
    }

    // ==================================================================================
    // ============================ ARRAY WRAPPING METHODS ==============================
    // ==================================================================================

    /**
     * Wrap the given value in an array
     * @param value (byte) - value to wrap
     * @return (byte[]) - array with one element (value)
     */
    public static byte[] wrap(byte value){
        return new byte[]{value};
    }

    // ==================================================================================
    // ========================== INTEGER MANIPULATION METHODS ==========================
    // ==================================================================================

    /**
     * Create an Integer using the given array. The input needs to be considered
     * as "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param bytes (byte[]) - Array of 4 bytes
     * @return (int) - Integer representation of the array
     * @throws AssertionError if the input is null or the input's length is different from 4
     */
    public static int toInt(byte[] bytes){
        assert(bytes != null) : "Bytes input is null";
        assert(bytes.length == 4) : "Input's length is different from 4";

        int result = 0;
        for(int iByte = 0; iByte < 4; iByte++){
            result <<= 8;
            result |= (bytes[iByte] & 0x00_00_00FF);
        }
        return result;
    }

    /**
     * Separate the Integer (word) to 4 bytes. The Memory layout of this integer is "Big Endian"
     * (See handout for the definition of "Big Endian")
     * @param value (int) - The integer
     * @return (byte[]) - Big Endian representation of the integer
     */
    public static byte[] fromInt(int value){
        byte[] result = new byte[4];
        for(int iByte = 0; iByte < 4; iByte++){
            result[iByte] = (byte)((value >> ((3 - iByte) * 8)) & 0x00_00_00_FF );
        }
        return result;
    }

    // ==================================================================================
    // ========================== ARRAY CONCATENATION METHODS ===========================
    // ==================================================================================

    /**
     * Concatenate a given sequence of bytes and stores them in an array
     * @param bytes (byte ...) - Sequence of bytes to store in the array
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     */
    public static byte[] concat(byte ... bytes){
        assert bytes != null : "Input is null";
        return bytes;
    }

    /**
     * Concatenate a given sequence of arrays into one array
     * @param tabs (byte[] ...) - Sequence of arrays
     * @return (byte[]) - Array representation of the sequence
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null.
     */
    public static byte[] concat(byte[] ... tabs){
        assert tabs != null : "Input is null";
        int resultLength = 0;
        for(byte[] tab : tabs){
            assert tab != null : "One of the inner arrays of input is null";
            resultLength += tab.length;
        }

        byte[] result = new byte[resultLength];
        int currPos = 0;

        for (byte[] tab : tabs) {
            for (byte currByte : tab) {
                result[currPos] = currByte;
                currPos++;
            }
        }

        return result;
    }

    // ==================================================================================
    // =========================== ARRAY EXTRACTION METHODS =============================
    // ==================================================================================

    /**
     * Extract an array from another array
     * @param input (byte[]) - Array to extract from
     * @param start (int) - Index in the input array to ézstart the extract from
     * @param length (int) - The number of bytes to extract
     * @return (byte[]) - The extracted array
     * @throws AssertionError if the input is null or start and length are invalid.
     * start + length should also be smaller than the input's length
     */
    public static byte[] extract(byte[] input, int start, int length){
        assert input != null : "Input is null";
        assert start >= 0 && start < input.length : "Start index is invalid";
        assert length >= 0 && length <= input.length : "Length of sub-array is invalid";
        assert start + length <= input.length : "End index is invalid";

        byte[] result = new byte[length];
        for(int iByte = 0; iByte < length; iByte++){
            result[iByte] = input[start + iByte];
        }
        return result;
    }

    /**
     * Create a partition of the input array.
     * (See handout for more information on how this method works)
     * @param input (byte[]) - The original array
     * @param sizes (int ...) - Sizes of the partitions
     * @return (byte[][]) - Array of input's partitions.
     * The order of the partition is the same as the order in sizes
     * @throws AssertionError if one of the parameters is null
     * or the sum of the elements in sizes is different from the input's length
     */
    public static byte[][] partition(byte[] input, int ... sizes) {
        assert input != null : "Input is null";
        assert sizes != null : "Sizes tab is null";

        int lengthSum = 0;
        byte[][] result = new byte[sizes.length][];

        for(int iSize = 0; iSize < sizes.length; iSize++){
            result[iSize] = extract(input, lengthSum, sizes[iSize]);
            lengthSum += sizes[iSize];
        }

        assert lengthSum == input.length : "The sum of the elements in sizes is different from the input's length";

        return result;
    }

    // ==================================================================================
    // ============================== ARRAY FORMATTING METHODS ==========================
    // ==================================================================================

    /**
     * Format a 2-dim integer array
     * where each dimension is a direction in the image to
     * a 2-dim byte array where the first dimension is the pixel
     * and the second dimension is the channel.
     * See handouts for more information on the format.
     * @param input (int[][]) - image data
     * @return (byte [][]) - formatted image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     */
    public static byte[][] imageToChannels(int[][] input){
        assert input != null : "Input is null";
        assert input[0] != null : "One of the inner arrays is null";
        int height = input.length;
        int width = input[0].length;

        byte[][] result = new byte[height*width][];

        for(int iLig = 0; iLig < input.length; iLig++){
            assert input[iLig] != null : "One of the inner arrays is null";
            assert input[iLig].length == width : "Two of the inner arrays does not have the same length";
            for(int iCol = 0; iCol < input[iLig].length; iCol++){
                byte[] currARGB = fromInt(input[iLig][iCol]);
                byte[] currRGBA = new byte[]{currARGB[1], currARGB[2], currARGB[3], currARGB[0]};
                result[iLig * width + iCol] = currRGBA;
            }
        }
        return result;
    }

    /**
     * Format a 2-dim byte array where the first dimension is the pixel
     * and the second is the channel to a 2-dim int array where the first
     * dimension is the height and the second is the width
     * @param input (byte[][]) : linear representation of the image
     * @param height (int) - Height of the resulting image
     * @param width (int) - Width of the resulting image
     * @return (int[][]) - the image data
     * @throws AssertionError if the input is null
     * or one of the inner arrays of input is null
     * or input's length differs from width * height
     * or height is invalid
     * or width is invalid
     */
    public static int[][] channelsToImage(byte[][] input, int height, int width){
        assert input != null : "Input is null";
        assert height >= 0 && width >= 0 : "Height or width is invalid";
        assert input.length == height * width : "Input's length differs from width * height";

        int[][] result = new int[height][width];

        for(int iLig = 0; iLig < height; iLig++){
            for(int iCol = 0; iCol < width; iCol++){
                assert input[iLig * width + iCol] != null : "One of the pixel is null";
                assert input[iLig * width + iCol].length == 4 : "One of the pixel is invalid";
                byte[] currRGBA = input[iLig * width + iCol];
                byte[] currARGB = new byte[]{currRGBA[3], currRGBA[0], currRGBA[1], currRGBA[2]};
                result[iLig][iCol] = toInt(currARGB);
            }
        }

        return result;
    }

}