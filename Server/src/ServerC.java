import java.io.*;
import java.net.Socket;
import java.util.*;

class ServerC extends Thread {

    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;

    public ServerC(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        start();
    }


    public List<String> createListWithFilesName(File folder) {
        List<String> listWithFilesName = new ArrayList<>();
        Queue<File> fileTree = new PriorityQueue<>();
        Collections.addAll(fileTree, folder.listFiles());
        while (!fileTree.isEmpty()) {
            File currentFile = fileTree.remove();
            if (currentFile.isDirectory()) {
                Collections.addAll(fileTree, currentFile.listFiles());
            } else {
                listWithFilesName.add(currentFile.getPath());
            }
        }
        return listWithFilesName;
    }

    public void createIndex (Map<String, HashSet<String>> index, int amountOfThreads,ThreadIndexBuilder [] threadArray){
        for (int i = 0; i < amountOfThreads; i++) {
            int finalI = i;

            for (Map.Entry<String, HashSet<String>> entry : threadArray[finalI].getResult().entrySet()) {
                String key = entry.getKey();
                HashSet<String> value = entry.getValue();
                if (index.containsKey(key)) {
                    index.computeIfAbsent(key, w -> new HashSet<>()).addAll(value);
                } else {
                    index.put(key, value);
                }
            }
        }
    }

    public String searchingFunction(String phraseForSearching, Map<String, HashSet<String>> index){
        String message = "";
        HashSet<String> setOfValuesToCompareFirst = new HashSet<>();
        HashSet<String> setOfValuesToCompareSecond = new HashSet<>();
        String[] wordsForSearching;
        String error = null;
        String delimeter = " ";
        wordsForSearching = phraseForSearching.split(delimeter);
        for (String word : wordsForSearching) {
            if (index.containsKey(word)) {
                message += word + " " + index.get(word) + "\n" + "\n";
                if (error == null) {
                    setOfValuesToCompareSecond.addAll(index.get(word));
                    if (setOfValuesToCompareFirst.isEmpty()) {
                        setOfValuesToCompareFirst.addAll(setOfValuesToCompareSecond);
                    } else {
                        setOfValuesToCompareSecond.retainAll(setOfValuesToCompareFirst);
                        if (setOfValuesToCompareSecond.isEmpty()) {
                            error = "No full matches" + "\n";
                        } else {
                            message += phraseForSearching + " " + setOfValuesToCompareSecond;
                        }
                    }
                }


            } else message += "This word -  " + word + " doesn't exist in the files" + "\n" + "\n";
        }
        return  message;
    }

    @Override
    public void run() {
        try {
            while (true) {

                File folder = new File("./data");
                String phraseForSearching = this.in.readLine();
                String amountOfThreadsStr = this.in.readLine();
                int amountOfThreads = Integer.parseInt(amountOfThreadsStr);
                List<String> listFilesName = createListWithFilesName(folder);
                long time = System.currentTimeMillis();
                ThreadIndexBuilder threadArray[] = new ThreadIndexBuilder[amountOfThreads];
                for (int i = 0; i < amountOfThreads; i++) {
                    threadArray[i] = new ThreadIndexBuilder(listFilesName, listFilesName.size() / amountOfThreads * i, i == (amountOfThreads - 1) ? listFilesName.size() : listFilesName.size() / amountOfThreads * (i + 1));
                    threadArray[i].start();
                }
                for (int i = 0; i < amountOfThreads; i++) {
                    threadArray[i].join();
                }
                System.out.println("Parallel time:" + (System.currentTimeMillis() - time));

                Map<String, HashSet<String>> index = new HashMap();
                createIndex(index, amountOfThreads, threadArray);

                for (ServerC vr : Server.serverList) {
                    vr.send(searchingFunction(phraseForSearching, index));
                }
            }

        } catch (IOException | InterruptedException e) {
            System.out.print(e);

        }
    }

    private void send(String msg) {
        try {
            out.write(msg + "\n" + "\n");
            out.flush();
        } catch (IOException ignored) {
        }
    }
}
