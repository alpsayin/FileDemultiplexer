/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filedemultiplexer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author Alp Sayin
 */
public class FileDemultiplexer implements Runnable
{
    public static final int READ_BUFFER_SIZE = 4*1024*1024;
    public static final int WRITE_BUFFER_SIZE = READ_BUFFER_SIZE/8;
    private String inputFilename;
    private String outputFilenamePattern;
    private String outputFilenameExtension;
    private String outputFileLocation;
    private int demuxCount;
    private long deltaTime;
    private int read_buffer_size = READ_BUFFER_SIZE;
    private int write_buffer_size = WRITE_BUFFER_SIZE;
    private ArrayList<ActionListener> actionListeners = new ArrayList<ActionListener>();
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
        this.read_buffer_size = read_buffer_size*1024;
        this.write_buffer_size = write_buffer_size*1024;
    }
    public void demultiplex() throws Exception
    {
        deltaTime = System.currentTimeMillis();
        File inputFile = new File(getInputFilename());
        OutputFile[] outputFiles = new OutputFile[getDemuxCount()];
        for(int i=0; i<getDemuxCount(); i++)
        {
            outputFiles[i] = new OutputFile(this.getOutputFileLocation()+File.separator+getOutputFilenamePattern()+(i+1)+"."+getOutputFilenameExtension());
        }
        FileInputStream fis = new FileInputStream(inputFile);
        BufferedInputStream bis = new BufferedInputStream(fis, this.getRead_buffer_size());
        byte[] nextBytes = new byte[this.getRead_buffer_size()];
        boolean keepReading = true;
        int fileIndex = 0;
        int totalBytesRead = 0;
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
                for(int j=0; j<bits.length; j++)
                {
                    outputFiles[fileIndex].writeBit(bits[j]);
                    fileIndex++;
                    if(fileIndex >= outputFiles.length)
                        fileIndex = 0;
                }
            }
            totalBytesRead += bytesRead;
            final ActionEvent ae = new ActionEvent(this, 0, "FileDemultiplexingStatusUpdate "+totalBytesRead+"/"+inputFile.length());        
            for(ActionListener al : actionListeners)
            {
                final ActionListener fal = al;
                Thread t = new Thread(new Runnable()
                {

                    @Override
                    public void run()
                    {
                        fal.actionPerformed(ae);
                    }
                });
                t.start();
            }
        }
        bis.close();
        for(int i=0; i<getDemuxCount(); i++)
        {
            outputFiles[i].close();
        }
        
        deltaTime = System.currentTimeMillis() - deltaTime;
        long deltaTimeMinutes = (deltaTime/1000)/60;
        long deltaTimeSeconds = (deltaTime/1000)%60;
        long deltaTimeMillis = deltaTime%1000;
        
        ActionEvent ae = new ActionEvent(this, 0, "FileDemultiplexingSuccess minutes:"+deltaTimeMinutes+" seconds:"+deltaTimeSeconds+" milliseconds:"+deltaTimeMillis);
        for(ActionListener al : actionListeners)
        {
            al.actionPerformed(ae);
        }
    }
    @Override public void run()
    {
        try
        {
            this.demultiplex();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    
    public boolean addActionListener(ActionListener al)
    {
        return this.actionListeners.add(al);
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
            byteBuf = ((byteBuf >> 1) & 0xFF);
            byteBuf |= (bit << 7);
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
            while(this.bitCounter != 0)
            {
                writeBit(0);
            } //zero-pad the remaining byte
            this.bos.close();
        }
    }
    private static int getBit(int data, int position)
    {
        return (data & (1 << position)) >> (position);
    }
    private static int[] get8Bits(int data)
    {
        return getNBits(data, 0, 8);
    }
    private static int[] getNBits(int data, int offset, int nbits)
    {
        int[] retVal = new int[nbits];
        for(int i=offset; i<offset+nbits; i++)
        {
            retVal[i] = ((data & (1 << (i))) >> (i)) & 0x1;
        }
        return retVal;
    }
    private static int[] getBits(byte[] data, int numOfBits) throws Exception
    {
        if(numOfBits % 8 != 0)
            throw new Exception("GetBits can only work for numOfBits which are multiple of 8");
        if(numOfBits/8 != data.length)
            throw new Exception("Too much or too less data input to getBits");
        int[] retVal = new int[numOfBits];
        int byteIndex = 0;
        for(int i=0; i<numOfBits; i++)
        {
            retVal[i] = ((data[byteIndex] & (1 << i)) >> i) & 0x1;
            if(i+1 % 8 == 0)
                byteIndex++;
        }
        return retVal;
    }
}
