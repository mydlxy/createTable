import myd.utils.Convert;
import myd.utils.EntryClassLoader;
import org.junit.Test;
import sun.misc.Launcher;
import sun.misc.URLClassPath;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

/**
 * @author myd
 * @date 2022/10/27  22:11
 */

public class ReflectTest {


    /**
     *
     * 测试bootstrapClassloader，appClassloader，extensionClassloader加载了哪些类
     *
     * @throws Exception
     */
    @Test
    public void testClassPath() throws Exception {

        /**bootstrapClassloader*/
        URL[] bootstrapClassloader = Launcher.getBootstrapClassPath().getURLs();

        for (URL url : bootstrapClassloader) {
            System.out.println(url.toExternalForm());
        }
        System.out.println("++++++++++++++++++++++++++++++++");

            /**appClassloader*/
        ClassLoader appClass = getClass().getClassLoader();
        System.out.println(appClass.getClass().getName());
        Field ucp = appClass.getClass().getDeclaredField("ucp");

        ucp.setAccessible(true);

        URLClassPath up = (URLClassPath) ucp.get(appClass);

        URL[] urLs = up.getURLs();

        for (URL urL : urLs) {
            System.out.println(urL.toExternalForm());
        }

        System.out.println("++++++++++++++++++++++++++++++++");
        /**extensionClassloader*/

        ClassLoader extClassLoader = getClass().getClassLoader().getParent();
        System.out.println(extClassLoader.getClass().getName());
        Method extDirs = extClassLoader.getClass().getDeclaredMethod("getExtDirs");

        extDirs.setAccessible(true);

        File[] files = (File[]) extDirs.invoke(null, null);

        for (File file : files) {
            System.out.println(file.toURI().toURL().toExternalForm());
        }
    }


    /**
     *
     *
     * 测试创建SQL语句
     * @throws Exception
     */
    @Test
    public void testCreateTable() throws Exception {
        String path = "/E:/Project/IDEA/20221026_erp_cn/JSH_ERP/jshERP-boot/target/classes";
        String packageLoad = "com.jsh.erp.datasource.entities";
        Convert.createTable("jsh_","Example",path,packageLoad,"./sql_01");

    }


    /**
     *
     * 测试加载 指定位置的class文件
     * @throws Exception
     */
    @Test
    public void testClassFile() throws Exception {

        String root = "/E:/Project/IDEA/20221026_erp_cn/JSH_ERP/jshERP-boot/target/classes";

        String packages = "com/jsh/erp/datasource/entities";

        URL url = new URL("file:"+root);

        EntryClassLoader  classLoader =EntryClassLoader.getClassLoader(new URL[]{new File(root).toURI().toURL()});

        classLoader.load(packages);

    }


    /**
     *
     *
     * 测试加载指定jar
     * @throws Exception
     */
        @Test
        public void testJAR() throws Exception {

            String jar = "/E:/Project/IDEA/202208/0819/spring-mini-0819/ioc-aop/target/ioc-aop-1.0-SNAPSHOT.jar";

            EntryClassLoader  classLoader =EntryClassLoader.getClassLoader(new URL[]{new File(jar).toURI().toURL()});

            classLoader.load(null);

        }




}
