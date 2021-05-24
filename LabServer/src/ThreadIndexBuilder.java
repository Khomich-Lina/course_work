import java.io.*;
import java.util.*;


public class ThreadIndexBuilder extends  Thread{
    List<String> list;
    int startIndex;
    int finishIndex;
    Map<String, HashSet<String>> indexTemporary = new HashMap();
    public ThreadIndexBuilder(List<String> listOfFilesName, int startIndex, int finishIndex){
        this.list = new ArrayList<String>(listOfFilesName);
        this.startIndex = startIndex;
        this.finishIndex= finishIndex;

    }

    public Map<String, HashSet<String>> getResult(){return indexTemporary ;}
    @Override
    public void run(){
        List<String> arrlist2 = list.subList(startIndex,finishIndex);
        for (String file : arrlist2){
            String line;
            String nameFile = file.replaceAll(".\\\\data\\\\", "");
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                while ((line = reader.readLine()) != null) {
                    String[] words = line.split(" ");
                    List<String> listOfWords = Arrays.asList(words);
                    for (String word : listOfWords) {
                        word=word.replaceAll("[,./>]","");
                        word=word.replaceAll("<br","");
                        if(word == null) listOfWords.remove(word);
                        word = word.toLowerCase(Locale.ROOT);
                        indexTemporary.computeIfAbsent(word, w -> new HashSet<>()).add(nameFile);
                    }
                }
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

}
