import java.io.*;

public class App {
    public static void main(String[] args) throws Exception {

        File folder = new File("C:\\FCAI Year 3 2022-2023\\GITHUB\\Information-Retrieval\\New folder");
        //File folder = new File("D:\\fcai\\Thirdlevel2\\IR\\ASS\\ir\\Information-Retrieval\\New folder");
        File[] fileList = folder.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            String fileName = fileList[i].getName();
            DataInputStream df = new DataInputStream(new FileInputStream(fileList[i]));
            String line;
            while ((line = df.readLine()) != null) {
                //Input: "The quick brown fox jumps over the lazy dog."
                //Output: ["The", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog"]
                String[] words = line.split("\\W+");
                System.out.println("\n"+"\n"+"Words in file #"+i);
                for (String word : words) {
                    System.out.println(word);
                }
            }
        }
    }
        
       

}
