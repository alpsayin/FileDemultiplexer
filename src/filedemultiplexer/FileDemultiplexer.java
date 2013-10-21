/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filedemultiplexer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Alp Sayin
 */
public class FileDemultiplexer
{
    public static final int READ_BUFFER_SIZE = 4*1024*1024;
    public static final int WRITE_BUFFER_SIZE = READ_BUFFER_SIZE/8;
    private String inputFilename;
    private String outputFilenamePattern;
    private String outputFilenameExtension;
    private String outputFileLocation;
    private int demuxCount;
    private int read_buffer_size = READ_BUFFER_SIZE;
    private int write_buffer_size = WRITE_BUFFER_SIZE;
    public FileDemultiplexer(String inputFilename, String outputFilenamePattern, String outputFilenameExtension, int demuxCount)
    {
        this.inputFilename = inputFilename;
        this.outputFilenamePattern = outputFilenamePattern;
        this.outputFilenameExtension = outputFilenameExtension;
        this.outputFileLocation = ".";
        this.demuxCount = demuxCount;
    }
    public FileDemultiplexer(String inputFilename, String outputFilenamePattern, String outputFilenameExtension, String outputFileLocation, int demuxCount, int read_buffer_size, int write_buffer_size)
    {
        this.inputFilename = inputFilename;
        this.outputFilenamePattern = outputFilenamePattern;
        this.outputFilenameExtension = outputFilenameExtension;
        this.outputFileLocation = outputFileLocation;
        this.demuxCount = demuxCount;
        this.read_buffer_size = read_buffer_size;
        this.write_buffer_size = write_buffer_size;
    }
    public void demultiplex() throws Exception
    {
        File inputFile = new File(getInputFilename());
        OutputFile[] outputFiles = new OutputFile[getDemuxCount()];
        for(int i=0; i<getDemuxCount(); i++)
        {
            outputFiles[i] = new OutputFile(this.getOutputFileLocation()+File.separator+getOutputFilenamePattern()+i+"."+getOutputFilenameExtension());
        }
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis, this.getRead_buffer_size());
        byte[] nextBytes = new byte[4*1024*1024];
        boolean keepReading = true;
        while(keepReading)
        {
            int bytesRead = bis.read(nextBytes);
            if(bytesRead == -1)
            {
                System.out.println("End of input file");
                keepReading = false;
                break;
            }
            for(int i=0; i<bytesRead; i++)
            {
                byte nextByte = nextBytes[i];
                int[] bits = get8Bits(nextByte);
                for(int j=0; j<getDemuxCount(); j++)
                {
                    outputFiles[j].writeBit(bits[j]);
                }
            }
        }
        bis.close();
        for(int i=0; i<getDemuxCount(); i++)
        {
            outputFiles[i].close();
        }
    }

    /**
     * @return the inputFilename
     */
    public String getInputFilename()
    {
        return inputFilename;
    }

    /**
     * @param inputFilename the inputFilename to set
     */
    public void setInputFilename(String inputFilename)
    {
        this.inputFilename = inputFilename;
    }

    /**
     * @return the outputFilenamePattern
     */
    public String getOutputFilenamePattern()
    {
        return outputFilenamePattern;
    }

    /**
     * @param outputFilenamePattern the outputFilenamePattern to set
     */
    public void setOutputFilenamePattern(String outputFilenamePattern)
    {
        this.outputFilenamePattern = outputFilenamePattern;
    }

    /**
     * @return the outputFilenameExtension
     */
    public String getOutputFilenameExtension()
    {
        return outputFilenameExtension;
    }

    /**
     * @param outputFilenameExtension the outputFilenameExtension to set
     */
    public void setOutputFilenameExtension(String outputFilenameExtension)
    {
        this.outputFilenameExtension = outputFilenameExtension;
    }

    /**
     * @return the demuxCount
     */
    public int getDemuxCount()
    {
        return demuxCount;
    }

    /**
     * @param demuxCount the demuxCount to set
     */
    public void setDemuxCount(int demuxCount)
    {
        this.demuxCount = demuxCount;
    }

    /**
     * @return the read_buffer_size
     */
    public int getRead_buffer_size()
    {
        return read_buffer_size;
    }

    /**
     * @param read_buffer_size the read_buffer_size to set
     */
    public void setRead_buffer_size(int read_buffer_size)
    {
        this.read_buffer_size = read_buffer_size;
    }

    /**
     * @return the write_buffer_size
     */
    public int getWrite_buffer_size()
    {
        return write_buffer_size;
    }

    /**
     * @param write_buffer_size the write_buffer_size to set
     */
    public void setWrite_buffer_size(int write_buffer_size)
    {
        this.write_buffer_size = write_buffer_size;
    }

    /**
     * @return the outputFileLocation
     */
    public String getOutputFileLocation()
    {
        return outputFileLocation;
    }

    /**
     * @param outputFileLocation the outputFileLocation to set
     */
    public void setOutputFileLocation(String outputFileLocation)
    {
        this.outputFileLocation = outputFileLocation;
    }
    private class OutputFile
    {
        private File file;
        private FileOutputStream fos;
        private BufferedOutputStream bos;
        private int byteBuf;
        private int bitCounter;
        public OutputFile(String filename) throws FileNotFoundException, IOException
        {
            this.file = new File(filename);
            if(!this.file.exists())
                this.file.createNewFile();
            this.fos = new FileOutputStream(file);
            this.bos = new BufferedOutputStream(fos, getWrite_buffer_size());
            this.byteBuf = 0;
            this.bitCounter = 0;
        }
        public void writeBit(int bit) throws IOException
        {
            byteBuf = ((byteBuf << 1) & 0xFF);
            byteBuf |= bit;
            bitCounter = (bitCounter+1) % 8;
            if(bitCounter == 0)
            {
                this.bos.write(byteBuf);
            }
        }
        public void writeByte(int byt) throws IOException
        {
            this.bos.write(byt);
        }
        public void close() throws IOException
        {
            this.bos.close();
        }
    }
    private static int getBit(int data, int position)
    {
        return (data & (1 << position)) >> (position);
    }
    private static int[] get8Bits(int data)
    {
        int[] retVal = new int[8];
        retVal[0] = ((data & (1 << 0)) >> 0) & 0xff;
        retVal[1] = ((data & (1 << 1)) >> 1) & 0xff;
        retVal[2] = ((data & (1 << 2)) >> 2) & 0xff;
        retVal[3] = ((data & (1 << 3)) >> 3) & 0xff;
        retVal[4] = ((data & (1 << 4)) >> 4) & 0xff;
        retVal[5] = ((data & (1 << 5)) >> 5) & 0xff;
        retVal[6] = ((data & (1 << 6)) >> 6) & 0xff;
        retVal[7] = ((data & (1 << 7)) >> 7) & 0xff;
        return retVal;
    }
}
