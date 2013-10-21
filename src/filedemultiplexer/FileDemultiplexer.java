/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filedemultiplexer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 *
 * @author Alp Sayin
 */
public class FileDemultiplexer
{
    private String inputFilename;
    private String outputFilenamePattern;
    private String outputFilenameExtension;
    private int demuxCount;
    public FileDemultiplexer(String inputFilename, String outputFilenamePattern, String outputFilenameExtension, int demuxCount)
    {
        this.inputFilename = inputFilename;
        this.outputFilenamePattern = outputFilenamePattern;
        this.outputFilenameExtension = outputFilenameExtension;
        this.demuxCount = demuxCount;
    }
    public void demultiplex() throws Exception
    {
        File inputFile = new File(inputFilename);
        OutputFile[] outputFiles = new OutputFile[demuxCount];
        for(int i=0; i<demuxCount; i++)
        {
            outputFiles[i] = new OutputFile(outputFilenamePattern+i+"."+outputFilenameExtension);
        }
        FileInputStream fis = new FileInputStream(inputFile);
        while(true)
        {
            int nextByte = fis.read();
            if(nextByte == -1)
            {
                System.out.println("End of input file");
                break;
            }
            int[] bits = getBits(nextByte);
            for(int i=0; i<demuxCount; i++)
            {
                outputFiles[i].writeBit(bits[i]);
            }
        }
        fis.close();
        for(int i=0; i<demuxCount; i++)
        {
            outputFiles[i].close();
        }
    }
    private class OutputFile
    {
        private File file;
        private FileOutputStream fos;
        private int byteBuf;
        private int bitCounter;
        public OutputFile(String filename) throws FileNotFoundException, IOException
        {
            this.file = new File(filename);
            if(!this.file.exists())
                this.file.createNewFile();
            this.fos = new FileOutputStream(file);
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
                this.fos.write(byteBuf);
            }
        }
        public void writeByte(int byt) throws IOException
        {
            this.fos.write(byt);
        }
        public void close() throws IOException
        {
            this.fos.close();
        }
    }
    private static int getBit(int data, int position)
    {
        return (data & (1 << position)) >> (position);
    }
    private static int[] getBits(int data)
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
    public static void main(String args[]) throws Exception
    {
        String inputFilename = "inputFile.bin";//args[0];
        String outputFilenamePattern = "output";//args[1];
        String outputFilenameExtension = "bin";//args[2];
        final int demuxCount = 8; //Integer.parseInt(args[3]);
        
        final JFrame frame = new JFrame("File Demultiplexer");
        final JTextField patternField = new JTextField("output");
        final JTextField extensionField = new JTextField("misl");
        final JTextField inputField = new JTextField();
        final JPanel outputPanel = new JPanel(new BorderLayout());
        outputPanel.add("Center", patternField);
        outputPanel.add("East", extensionField);
        inputField.setEnabled(false);
        final JButton selectFileButton = new JButton("Browse");
        final JFileChooser fchooser = new JFileChooser();
        selectFileButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                int retVal = fchooser.showOpenDialog(frame);
                if(retVal == JFileChooser.APPROVE_OPTION)
                    inputField.setText(fchooser.getSelectedFile().getAbsolutePath());
            }
        });
        final JButton demuxButton = new JButton("DEMUX");
        demuxButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e){
                FileDemultiplexer fdm = new FileDemultiplexer(inputField.getText(), patternField.getText(), extensionField.getText(), demuxCount);
                try
                {
                    fdm.demultiplex();
                }
                catch(Exception ex)
                {
                    JOptionPane.showMessageDialog(frame, ex.getMessage(), ex.getCause().toString(), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        frame.setLayout(new BorderLayout());
        frame.add("Center", inputField);
        frame.add("East", selectFileButton);
        frame.add("North", outputPanel);
        frame.add("South", demuxButton);
        frame.setSize(320, 240);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
