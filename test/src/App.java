import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class App {
    public static void main(String[] args) throws Exception {

        File folder = new File("D:\\fcai\\Thirdlevel2\\IR\\ASS\\ir\\Information-Retrieval\\New folder");
        File[] fileList = folder.listFiles();

        for (int i = 0; i < fileList.length; i++) {
            String fileName = fileList[i].getName();
            //int docId = Integer.parseInt(fileName.substring(0, fileName.indexOf("."))); //i
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileList[i]));
                String line;
                while ((line = br.readLine()) != null) {
                    //Input: "The quick brown fox jumps over the lazy dog."
                    //Output: ["The", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog"]
                    String[] words = line.split("\\W+");
                    for (String word : words) {
                        System.out.println(word);
                    }
                    
                    
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
        
       

}
