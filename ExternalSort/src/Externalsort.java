import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class Externalsort {
    public static void main(String args[]) {
        try {
            int count3 = 0;
            RandomAccessFile raf = new RandomAccessFile(new File(
                args[0]), "r");
            RandomAccessFile runFile = new RandomAccessFile(new File("runFile.bin"), "rw");
            RandomAccessFile outputFile = new RandomAccessFile(new File("outputFile.bin"), "rw");
            runFile.setLength(0);
            outputFile.setLength(0);
            LinkedList<Long> runInfo = new LinkedList<Long>();
            byte initHeap[] = new byte[65536];
            raf.readFully(initHeap);
            ByteBuffer b = ByteBuffer.wrap(initHeap);
            MinHeap records = new MinHeap();
            int count1 = -1;
            for (int c = 0; c < 65536; c+= 16)
            {
                records.add(new Record(b.getLong(), b.getDouble()));
//                count1++;
            }
//            System.out.println(count1);
            records.buildMinHeap();
            runInfo.add(runFile.getFilePointer());
            runInfo.add((long)0);
            int outIndex = 0;
            while (!records.isEmpty() && raf.getFilePointer() < raf.length())
            {
                initHeap = new byte[8192];
                raf.readFully(initHeap);
                b = ByteBuffer.wrap(initHeap);
                Record inputBuffer[] = new Record[512];
                for (int c = 0; c < 8192; c+= 16)
                {
                    inputBuffer[c/16] = new Record(b.getLong(), b.getDouble()); 
//                    System.out.println(c/16);
                }
                Record [] outputBuffer = new Record[512]; //This needs to go at some point, just wrap the out arr VVVVV
                while (outIndex < 512)
                {
                    for (int c = 0; c < inputBuffer.length; c++)
                    {
                        outputBuffer[outIndex] = records.remove(0);
                        if (inputBuffer[c].compareTo(outputBuffer[outIndex]) > 0)
                        {
//                            System.out.println("Data[0]:" + records.data[0].getKey());
                            records.modify(0, inputBuffer[c]);
//                            System.out.println("Afer[0]:" + records.data[0].getKey() + "\t" + count3);
                            count3++;
                        }
                        else
                        {
                            records.removeMinRS(inputBuffer[c]);
//                            System.out.println("Afer[0]:" + records.data[0].getKey() + "\t" + count3);
                            count3++;
                        }
                        inputBuffer[c] = null;
                        records.siftDown(0);
                        outIndex++;
                    }
                }
                outIndex = 0;
              byte [] output = new byte [512*16];
              ByteBuffer out = ByteBuffer.wrap(output);
//              System.out.println(outputBuffer.length);
              for (int c = 0; c < outputBuffer.length; c++)
              {
//                  System.out.println(outputBuffer[c].getId() + "\t" + outputBuffer[c].getKey() + "\t" + count1);
                  count1++;
                  out.putLong(outputBuffer[c].getId());
                  out.putDouble(outputBuffer[c].getKey());
              }
              runFile.write(output);
              outputBuffer = new Record[512];
            }
            runInfo.add(runFile.getFilePointer());
            runInfo.add((long)0);
            records.resetLast();
            records.buildMinHeap();
            while (!records.isEmpty())
            {
                byte[] output = new byte[512*16];
                ByteBuffer heapOut = ByteBuffer.wrap(output);
                if (!records.isEmpty())
                {
                    for (int c = 0; c < output.length; c+=16)
                    {
                        Record r = records.removeMin();
                        if (r != null)
                        {
//                            System.out.println(r.getId() + "\t" + r.getKey() + "\t" + count1);
                            count1++;
                            heapOut.putLong(r.getId());
                            heapOut.putDouble(r.getKey());
                        }
                    }
                }
                runFile.write(output);           
            }
            raf.close();
            runFile.seek(0);
            byte [] runFileTest = new byte[131072];
            b = ByteBuffer.wrap(runFileTest);
//            System.out.println(runFile.length());
            runFile.readFully(runFileTest);
            LinkedList<String> output = new LinkedList<String>();
            int count2 = 0;
            for (int c = 0; c < runFileTest.length; c+=16)
            {
                output.add(b.getLong() + "\t" + b.getDouble() + "\t" + count2 + "\n");
                count2++;
            }
            Path file = Paths.get("runFileOutput.txt");
            Files.write(file, output, Charset.forName("UTF-8"));
            //I have jumps in my data when this runs right now. Gotta make sure runs are happening sequentially correctly
            int outIndex1 = 0;
            int count = 0;
            int [] runCount = new int[runInfo.size()/2];
            boolean skipComp = false;
            for (int c = 0; c < runCount.length; c++)
            {
                runCount[c] = 0;
            }
            int spot = 0;
            byte [] mergeArr = new byte[8192];
            Record [][] mergeInput = new Record[runInfo.size()/2][512];
            spot = 0;
            for (int c = 0; c < runInfo.size(); c+=2)
            {
                mergeArr = new byte[8192];
                runFile.seek(runInfo.get(c) + runCount[0]*8192);
//                System.out.println(runFile.getFilePointer());
                runFile.readFully(mergeArr);
                b = ByteBuffer.wrap(mergeArr);  
                for (int x = 0; x < mergeArr.length; x+=16)
                {
                    mergeInput[spot][x/16] = new Record(b.getLong(), b.getDouble());
                }
                spot++;
            }
            int lineCount = 0;
            while (outputFile.getFilePointer() < runFile.length() && runFile.getFilePointer() < runFile.length())
            {
//                for (int c = 0; c < runCount.length; c++)
//                {
//                    System.out.println(runCount[c]);
//                }
                Record [] outputBuffer = new Record[512];
                while (outIndex1 < outputBuffer.length)
                {
                    Record min = new Record(Long.MAX_VALUE, Double.MAX_VALUE);
                    int minIndex = 0;
                    for (int c = 0, i = 1; c < runInfo.size()/2; c++, i+=2) //This is iffy
                    {
                        if (runInfo.get(i) == 512)
                        {
                            runCount[c]++;
                            if (runCount[c] < 8)
                            {
                                mergeArr = new byte[8192];
                                runFile.seek(runInfo.get(i-1) + runCount[c]*8192);
//                                System.out.println(runFile.getFilePointer());
                                runFile.readFully(mergeArr);
                                b = ByteBuffer.wrap(mergeArr); 
                                spot = c;
                                for (int h = 0; h < mergeArr.length; h+=16)
                                {
                                    mergeInput[spot][h/16] = new Record(b.getLong(), b.getDouble());
                                }
                            }
                            else
                            {
                                skipComp = true;
                            }
                            runInfo.set(i, (long)0);
                        }
//                        if (outIndex < 10)
//                        System.out.println("Min: " + min.getKey() + "\tCheck: " + mergeInput[(int)(512*c + runInfo.get(i))].getKey());
                        if (!skipComp)
                        {
                            if (min.getKey() > mergeInput[c][runInfo.get(i).intValue()].getKey())
                            {
                                min = mergeInput[c][runInfo.get(i).intValue()];
                                minIndex = i;
                            }
                        }
                        else
                        {
                            min = mergeInput[c][runInfo.get(i).intValue()];
                            minIndex = i;
                        }
                    }
                    runInfo.set(minIndex, runInfo.get(minIndex) + 1);
                    outputBuffer[outIndex1] = min;
//                    System.out.println(min.getKey());
                    outIndex1++;
                }
                outIndex1 = 0;
                byte [] outArr = new byte[8192];
                ByteBuffer outFile = ByteBuffer.wrap(outArr);
                for (int c = 0; c < outArr.length; c+=16)
                {
                    if (c == 0)
                    {
                        System.out.print(outputBuffer[c/16].getId() + "\t" + outputBuffer[c/16].getKey() + "\t");
                        lineCount++;
                        if (lineCount == 5)
                        {
                            System.out.print("\n");
                            lineCount = 0;
                        }
                    }
                    count++;
                    outFile.putLong(outputBuffer[c/16].getId());
                    outFile.putDouble(outputBuffer[c/16].getKey());
                }
                outputFile.write(outArr);
            }
            runFile.close();
            byte [] outputTest = new byte[131072];
            b = ByteBuffer.wrap(outputTest);
            outputFile.seek(0);
            outputFile.readFully(outputTest);
            output = new LinkedList<String>();
            count2 = 0;
            for (int c = 0; c < outputTest.length; c+=16)
            {
                output.add(b.getLong() + "\t" + b.getDouble() + "\t" + count2 + "\n");
                count2++;
            }
            file = Paths.get("output.txt");
            Files.write(file, output, Charset.forName("UTF-8"));
            outputFile.close();
            
        }
        catch (FileNotFoundException e) {
            System.out.println("file not found");
        }
        catch (IOException e) {
            System.out.println("no file");
        }
    }
}
