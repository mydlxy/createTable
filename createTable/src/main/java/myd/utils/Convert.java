package myd.utils;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author myd
 * @date 2022/10/27  21:32
 */


@Slf4j
public class Convert {


    /**
     *
     *
     *
     * @param prefix   生成table的前缀
     * @param entry  entry类
     * @return
     */
    private static String entryMapTableName(String prefix,Class entry){

        if(entry.getSuperclass() != null && !entry.getSuperclass().equals(Object.class)){
            entry = entry.getSuperclass();
        }
        String entryName = entry.getSimpleName();
        StringBuilder sb = new StringBuilder(entryName.length()+3);
        for (int i = 0; i < entryName.length(); i++) {
            char c = entryName.charAt(i);
            if(c>='A' &&  c <= 'Z'){
                sb.append('_').append(Character.toLowerCase(c));
            }else{
                sb.append(c);
            }
        }
        return (prefix==null ? "":prefix)+sb.toString().substring(1);
    }


    private static final String EMPTY = " ";

    private static final String SPLIT = ",";

    /**
     * @param entry entry
     * @param cols 字段
     */
    private static void tableCols(Class entry,List<String> cols){

        if(entry.getSuperclass()!=null && !entry.getSuperclass().equals(Object.class)){
            Class superClass = entry.getSuperclass();
            tableCols(superClass,cols);
        }
        Field[] fields = entry.getDeclaredFields();
        for (Field field : fields) {
           String col = field.getName() + EMPTY + colType(field);
           cols.add(col);
        }
    }
    private static String colType(Field field){
        String typeName = field.getType().getSimpleName();

        switch (typeName){
            case "Character":
            case "char":
            case "String": return "varchar(20)";
            case "int":
            case "Integer":return "int";
            case "long" :
            case "Long" :return "bigint";
            case "BigDecimal": return "decimal(20,10)";
            case "boolean":
            case "Boolean":return "bit";
            case "Date":return "date";
            case "Byte": return "char(2)";
            case "Short": return "smallint";
            default:
                throw new RuntimeException("不支持生成这个类型:"+typeName);
        }


    }


    /**
     *
     *
     * @param prefix
     * @param filterStr
     * @param entryName
     * @return
     */
    private static String createSQL(String prefix,String filterStr,String entryName,String packageName)  {
//        if(entryName.contains("$")  || entryName.endsWith(filterStr))return null;
        if(filerClass(filterStr,entryName))return null;


        List<String>tableCols = new ArrayList<>();

        //使用自定义类加载器加载class文件；
        Class entryClass = null;
        try{
            String fullClassName = packageName+"."+entryName;
            entryClass = EntryClassLoader.getClass(fullClassName);
        }catch (ClassNotFoundException e){
//            Class.forName()
           log.error("classLoad error =>[{}]",packageName+"."+entryName,e);
        }
        String tableName = entryMapTableName(prefix, entryClass);
        try {
            tableCols(entryClass, tableCols);
        }catch (Exception e){
            log.info("col get false ,cause={}",e.getCause());
            return null;
        }

        return SQL(tableName,tableCols);



    }


    private static boolean filerClass(String filterStr,String name){
        if(filterStr == null)return false;
        if(name.contains("$"))return true;
        String[] filters = filterStr.split(",");
        for (String filter : filters) {
            if(name.endsWith(filter))return true;
        }
        return false;
    }


    public static void findEntryCreatTable(String prefix,String filterStr,String path,String loadPackage) throws Exception {

        EntryClassLoader classLoader = EntryClassLoader.getClassLoader(new URL[]{new File(path).toURI().toURL()});
        classLoader.load(loadPackage);
        String packagePath  =loadPackage==null ? null: loadPackage.replaceAll("\\.","/");
        String targetPath  = path + (packagePath == null? "" : "/"+packagePath);
        File dir = new File(targetPath);
        if(!dir.isDirectory())return ;
        String[] files = dir.list();
        int i=0;
        for (String file : files) {
            String entryName = file.substring(0,file.length()-6);
            String sql = createSQL(prefix,filterStr,entryName,loadPackage);
            if(sql != null && sql.startsWith("create")){
                System.out.println("++++++++++++++++++++");
                System.out.println(sql);
                System.out.println("++++++++++++++++++++");
                i++;
            }
        }
        System.out.println("create table number is :"+i);
    }




    public static void createTable(String prefix,String filterStr,String path,String loadPackage,String sqlPath)throws Exception {
        EntryClassLoader classLoader = EntryClassLoader.getClassLoader(new URL[]{new File(path).toURI().toURL()});
        classLoader.load(loadPackage);
        String packagePath =loadPackage==null ? null : loadPackage.replaceAll("\\.","/");
        String targetPath  = path + (packagePath == null? "" : "/"+packagePath);
        File dir = new File(targetPath);
//
        if(!dir.isDirectory())return ;
        String[] files = dir.list();
        int i=0;
        File sqlFile = new File(sqlPath);
        try(OutputStream out = new FileOutputStream(sqlFile,true)) {
            for (String file : files) {
                String entryName = file.substring(0, file.length() - 6);
                String sql = createSQL(prefix, filterStr, entryName, loadPackage);
                if (sql != null && sql.startsWith("create")) {
                    out.write(sql.getBytes(), 0, sql.length());
                }
            }
            out.flush();
        }catch (Exception e){

        }

        System.out.println("create table number is :"+i);



    }
    private static String SQL(String table,List<String> cols){
        StringBuilder sb = new StringBuilder("create table if not exists  "+ table+"(\n");
        cols.forEach(col->{

            if(col.matches("^id\\s.*") || col.matches("^"+table+"_?(I|i)(d|D)$")){
                sb.append(col + " primary key,\n");
            }else{
                sb.append(col+",\n");
            }
        });
        int last = sb.lastIndexOf(",");
        sb.replace(last,last+1,"\n);\n");
        return sb.toString();
     }

     @Test
     public void testSQL(){

        List<String> t  = new ArrayList<>();
         for (int i = 0; i <10 ; i++) {
             t.add(i+"");
         }
         t.add("name varchar(50)");
         t.add("id int");

         String table = "test";
         String sql = SQL(table,t);
        log.info("\ntable=>{}\nsql=>[{}]",table,sql);
     }


     @Test
     public void testCreateTable(){

        String prefix = "jsh_";
        String filter = "Example";
     }
}
