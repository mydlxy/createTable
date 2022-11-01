package myd.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author myd
 * @date 2022/10/28  22:30
 */


@Slf4j
public class EntryClassLoader extends URLClassLoader {

    static Map<String,Class> classes = new HashMap<>();

    private void add(String name,Class beanClass){
        classes.put(name,beanClass);
    }

    public  static Class  getClass(String name) throws ClassNotFoundException {

        Class target =  classes.get(name);

        if(target == null)
            throw new ClassNotFoundException(name + " not found error");

        return target;
    }

    /**
     *
     *
     *
     *
     * @param urls urls是jar的路径，或者class文件的根路径
     *
     *          *  e.g.
     *          *   /e:/qw/e/er/ty/sddfsd.jar
     *          *
     *          * 传入的URL参数：new URL[]{new File("/e:/qw/e/er/ty/sddfsd.jar").toURI().toURL()}
     *          *
     *          *
     *          * e.g.
     *          *
     *          * Account.class文件的路径
     *          * /e:/qw/e/er/ty/Account.class
     *          *类：Account的全限定名是：er.ty.Account
     *          * 那么根路径：/e:/qw/e
     *          *
     *          *
     *          * 传入的URL参数：
     *             new URL[]{new File("file:/e:/qw/e").toURI().toURL()}
     *          *
     *
     *
     * @param parent  父类加载器，一般是：ApplicationClassLoader
     */
    private EntryClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }


    private EntryClassLoader(URL[] urls){
        super(urls,getSystemClassLoader());
    }


    public static EntryClassLoader getClassLoader(URL[] urls){
        return new EntryClassLoader(urls);
    }


    /**
     *
     * @param loadPackage 指定加载类的路径；null，加载urLs下所有的类
     * @return
     */
    public void load(String loadPackage) throws Exception {
        URL[] urLs = getURLs();
        for (URL urL : urLs) {
            try{
                loadJAR(urL,loadPackage);
            }catch (Exception e){
                loadFile(urL, loadPackage);
            }

        }
    }

    /**
     *
     * @param jarURL
     * @param loadPackage
     * @throws Exception
     */
    public void loadJAR(URL jarURL,String loadPackage) throws Exception {

            JarFile jarFile = new JarFile(new File(jarURL.toURI()));
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                if (jarEntry.isDirectory()) {
                    continue;
                }
                String name = jarEntry.getName();
                if (!name.endsWith(".class")) {
                    continue;
                }

                // 转换 org/spring/AAA.class -> org.spring.AAA
                String tmp = name.substring(0, name.length() - 6);
                String res = StringUtils.replace(tmp, "/", ".");

                 load(res,loadPackage);



            }


    }

    /**
     *
     *
     * @param className 全限定类名
     * @param loadPackage 加载的包名路径
     * @return
     */
    public boolean loadOrNot(String className,String loadPackage){
        if("".trim().equals(loadPackage) || loadPackage == null)
            return true;
        else if(className.startsWith(loadPackage))
            return true;
        else
            return false;
    }


    /**
     *
     *
     * @param dirUrl
     * @param loadPackage
     * @throws Exception
     */
    public void loadFile(URL dirUrl,String loadPackage) throws Exception{
        String path = dirUrl.toURI().getPath();
        String loadPath =loadPackage==null ? "": loadPackage.replaceAll("\\.","/");
        File root =  new File(path+"/"+loadPath);
        String[] classDir = root.list();
        for (String className : classDir) {
            loadFile(path,null,className,loadPath);
        }



    }


    /**
     * @param root  项目的根路径
     * @param parent 包路径
     * @param fileName 文件名
     * @param loadPath 指定加载的class文件路径，或者指定包路径；
     * @throws ClassNotFoundException
     */
    public void loadFile(String root,String parent,String fileName,String loadPath) throws ClassNotFoundException {

        String fullFile = fullFileName(loadPath,parent,fileName);
        File file = new File(root+"/"+fullFile);

        if(!file.isDirectory() && fileName.endsWith(".class") ){
            String fullClassName = fullFile.substring(0,fullFile.length()-6).replaceAll("/","\\.");
//            load(fullClassName,loadPath);
            Class loadClass = loadClass(fullClassName);
            add(fullClassName,loadClass);
            log.info("load class =>{}",fullClassName);

        }

        if(file.isDirectory()){
            String[] fileNames = file.list();
            for (String name : fileNames) {
                loadFile(root,fullFile,name,loadPath);
            }
        }
    }


    /**
     *
     * @param className
     * @param packageName
     * @throws ClassNotFoundException
     */
    public void load(String className,String packageName) throws ClassNotFoundException {

        boolean load = loadOrNot(className,packageName);

        if(load){
            Class loadClass = loadClass(className);
            add(className,loadClass);
            log.info("load class =>{}",className);
        }

    }


    public String fullFileName(String loadPath,String parent,String fileName){
        if(loadPath == null || loadPath.trim().equals("")){

            if(parent == null || parent.equals(""))
                return fileName;
            else
                return parent + "/" + fileName;
        }

        else{

            if(parent == null || parent.equals(""))
                return loadPath + "/" + fileName ;
            else
                return loadPath + "/" + parent+fileName;

        }

    }



    public void printJARFile(Enumeration<JarEntry> entries){
        while(entries.hasMoreElements()){
            JarEntry jarEntry = entries.nextElement();
            if(jarEntry.isDirectory())
                System.out.println(jarEntry.getName()+"(包)");
            else
                System.out.println(jarEntry.getName());
        }

    }


}
