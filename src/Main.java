
import java.io.*;
import java.util.*;

/**
 * Created by fandexian on 15/12/26.
 */
public class Main {
    static int testnum = 0;

    private static Map<String,Integer> dou;//存放二元组的值以及出现的次数
    private static Map<String,Integer> tri;//存放三元组的值以及出现的次数
    private static Map<String,String> dou_tri;//存放二元组的值以及出现的次数

    private static int num_sentence;

    public static void main(String [] args)throws IOException{
        constructModel();//建造三元模型

        calculatedChaos();//计算混乱度M

        generateSentence();

    }

    private static void constructModel() throws IOException {

        FileReader fileReader = new FileReader("data.conll");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        dou = new HashMap<String,Integer>();
        tri = new HashMap<String,Integer>();
        dou_tri = new HashMap<String,String>();

        /*
        *临时的辅助变量
         */
        String character1 = "";//分别用于存储二元组和三元组的第一个词
        String character2 = "";//分别用于存储二元组和三元组的第二个词
        String[] data = null;//用于存储分词结果
        String str = null;//用于存储文件中读出的行
        int position = 0;//用于存放是开头的一句话的第几个词，如果是第二个词就开始进行二元存贮，如果是第三个词就开始进行三元存储

        while((str = bufferedReader.readLine())!=null){
            position++;
            data = str.split("	");
            if(data.length>1) {
                //data数组的元素数量大于1，说明此时不是一句话的结束。
                if (position == 1) {
                    character1 = data[1];
                } else if (position == 2) {
                    //如果position是2，此时二元组已经可以写入了。
                    character2 = data[1];
                    putDou(character1,character2);
                } else if (position > 2) {
                    //如果大于2，三元组开始写入
                    putTri(character1,character2,data[1]);
                    //二元组一样要写入
                    putDou(character1,character2);
                    //二元组和对应的三元组写入
                    if(position == 3) {
                        dou_tri.put("startstart" +  "	" + character1 + "	" + character2, character1 + "	" + character2 + "	" + data[1]);
                    }else{
                        dou_tri.put(character1 + "	" + character2, character1 + "	" + character2 + "	" + data[1]);

                    }


                    //向后滑动一个词
                    character1 = character2;
                    character2 = data[1];
                }
            }else{
                putDou(character1,character2);
                if(data.length>1) {
                    dou_tri.put(character1 + "	" + character2, character1 + "	" + character2 + "	" + data[1] + "	" + "end");
                }
                //此时，句子结束，句子总数++
                num_sentence++;
                //position置零
                position = 0;
            }
        }

        calculatedProbability();//计算概率
    }

    private static void putTri(String character1,String character2,String character3) {
        if (tri.containsKey(character1+"	"+character2+"	"+character3)) {
            int num = tri.get(character1+"	"+character2+"	"+character3);//当前对应key的value值
            tri.put(character1+"	"+character2+"	"+character3, ++num);
        }else{
            tri.put(character1+"	"+character2+"	"+character3,1);
            //System.out.println(character1 + " " + character2 + "   " + data[1]);
        }
    }

    private static void putDou(String character1,String character2) {

        if (dou.containsKey(character1+"	"+character2)) {
            //如果已经存储过，修改value值就好了
            int num = dou.get(character1+"	"+character2);//当前对应key的value值
            dou.put(character1+"	"+character2, ++num);
        }else{
            //如果没有存储过就把value值设为1
            dou.put(character1+"	"+character2,1);
        }
    }

    //计算三元除二元的概率58,10
    private static void calculatedProbability() throws IOException{

        Iterator it = tri.entrySet().iterator();

        FileWriter fileWriter = new FileWriter("probability.txt");
        while(it.hasNext()){
            Map.Entry tri_entry = (Map.Entry)it.next();
            String tri_key = (String) tri_entry.getKey();
            int tri_value = (Integer) tri_entry.getValue();

            String[] data = tri_key.split("	");
            String dou_key = data[0]+"	"+data[1];


            if(dou.containsKey(dou_key)){
                int dou_value = dou.get(dou_key);
                String line = tri_key+"<======>"+dou_key+" "+(double)tri_value/dou_value+"\n";
                //System.out.println(line);
                fileWriter.write(line);
            }

        }
    }

    //计算混乱度
    private static void calculatedChaos() throws IOException{
        FileReader fileReader = new FileReader("data.conll");
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        String character1 = "";
        String character2 = "";//分别用于存储二元组和三元组的第一个词和第二个词
        String[] data = null;//用于存储分词结果
        String str = null;//用于存储读出的一行
        int position = 0;//用于存放是开头的一句话的第几个词，如果是第二个词就开始进行二元存贮，如果是第三个词就开始进行三元存储
        String dou_key="",tri_key="";//用于存放出二元和三元
        double sentence_probability = 1;
        double l = 0;

        while((str = bufferedReader.readLine())!=null){
            position++;
            data = str.split("	");
            if(data.length>1) {
                //data数组的元素数量大于1，说明此行有数据。
                if (position == 1) {
                    character1 = data[1];
                } else if (position == 2) {
                    character2 = data[1];
                } else if (position > 2) {
                    dou_key = character1+"	"+character2;
                    tri_key = character1+"	"+character2+"	"+data[1];
                    if(tri.containsKey(tri_key) && dou.containsKey(dou_key)) {
                        //二元组三元组都存在
                        sentence_probability *= (double) tri.get(tri_key) / (double) dou.get(dou_key);
                        System.out.println(tri_key+"<========>"+dou_key+"    "+(double) tri.get(tri_key) / (double) dou.get(dou_key));
                        //System.out.println("正常情况下"+sentence_probability);
                    }else if(!tri.containsKey(tri_key) && dou.containsKey(dou_key)){
                        //二元组存在，三元组不存在
                        sentence_probability *= 1/(double) dou.get(dou_key);
                        System.out.println("三元组不存在情况下"+sentence_probability);

                    }else if(!tri.containsKey(tri_key) && !dou.containsKey(dou_key)){
                        //二元组三元组都不存在
                        sentence_probability *= 1;
                        System.out.println("二元三元组都不存在"+sentence_probability);
                    }

                    character1 = character2;
                    character2 = data[1];
                }
            }else{
                System.out.println("第"+testnum+"句话的概率合计为："+sentence_probability+"\n\n");
                l += Math.log(sentence_probability);
                testnum++;
                position = 0;
                sentence_probability = 1;
            }
        }
        System.out.println("l"+l+"      "+testnum+num_sentence);
        java.text.DecimalFormat df =new   java.text.DecimalFormat("#.00");
        double M = Double.parseDouble(df.format(Math.pow(2,-l/(num_sentence+1))));
        System.out.println("计算得到的混乱度值为"+M);
    }


    //生成句子
    private static void generateSentence() throws IOException {

        FileWriter fileWriter = new FileWriter("generatedSentences.txt");

        Random random = new Random();
        Iterator iterator = dou_tri.entrySet().iterator();
        for(int i = 0 ; i<100 ; i++){
            //从二元组中随机拿出一个，作为句子的开始单词
            while (iterator.hasNext()) {
                Map.Entry dou_tri_entry = (Map.Entry) iterator.next();
                //System.out.println(entry.getKey().toString().split("	")[0]);
                if (dou_tri_entry.getKey().toString().split("	")[0].equals("startstart")) {
                    //System.out.println("进来了");
                    //因为set本来就是无序的，就拿第一个为key开头为startstart的作为句子的起始
                    StringBuilder sentence = new StringBuilder();//句子
                    String key = (String) dou_tri_entry.getKey();//句子的开头在tri_dou Map里
                    String[] st = key.split("	");
                    sentence.append(st[1]+st[2]);//将句子开头两个字先写入
                    while (dou_tri.containsKey(key)) {
                        //如果以上次出现的两个字为value的Map存在，
                        // 那么对应的key就是该二元组对应的三元组，
                        //那么下个词也就找到了
                        String value = dou_tri.get(key);
                        String[] data = value.split("	");
                        key = data[1]+"	"+data[2];
                        sentence.append(data[2]);
                        if(data.length == 4){
                            break;
                        }
                    }
                    System.out.println("第"+i+"句话："+sentence);
                    fileWriter.write("第"+i+"句话："+sentence+"\n");
                    break ;
                }
            }
        }
    }
}

