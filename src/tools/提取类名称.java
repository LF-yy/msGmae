//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package tools;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class 提取类名称 {
    private static Map<String, ArrayList<String>> para_map = new HashMap();
    private static Map<String, String> return_map = new HashMap();
    private static ArrayList<String> para = new ArrayList();

    public 提取类名称() {
    }

    public static void testPropertiesAndMethods() {
        try {
            BeanInfo bi = Introspector.getBeanInfo(Book.class);
            PropertyDescriptor[] pds = bi.getPropertyDescriptors();
            MethodDescriptor[] mds = bi.getMethodDescriptors();
            System.out.println("------------propertities-------------");

            int i;
            String methodName;
            for(i = 0; i < pds.length; ++i) {
                methodName = pds[i].getName();
                System.out.println(methodName);
            }

            System.out.println("------------methods-------------");

            for(i = 0; i < mds.length; ++i) {
                methodName = mds[i].getName();
                Method method = mds[i].getMethod();
                System.out.println(methodName);
                Class<?>[] parameterTypes = method.getParameterTypes();
                Class[] var7 = parameterTypes;
                int var8 = parameterTypes.length;

                for(int var9 = 0; var9 < var8; ++var9) {
                    Class<?> clas = var7[var9];
                    String parameterName = clas.getName();
                    System.out.println("参数名称:" + parameterName);
                }
            }
        } catch (IntrospectionException var12) {
            var12.printStackTrace();
        }

    }

    public static void 遍历方法信息(String pkgName, boolean isClear) {
        try {
            Class clazz = Class.forName(pkgName);
            Method[] methods = clazz.getMethods();
            if (isClear) {
                para_map.clear();
                return_map.clear();
            }

            Method[] var4 = methods;
            int var5 = methods.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                Method method = var4[var6];
                String methodName = method.getName();
                String return_type = method.getGenericReturnType().toString();
                ArrayList<String> Filter_list = new ArrayList(Arrays.asList("wait", "equals", "toString", "hashCode", "getClass", "notify", "notifyAll"));
                if (!Filter_list.contains(methodName)) {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    para.clear();
                    Class[] var12 = parameterTypes;
                    int var13 = parameterTypes.length;

                    for(int var14 = 0; var14 < var13; ++var14) {
                        Class<?> clas = var12[var14];
                        String parameterName = clas.getName();
                        if (parameterName.equals("java.lang.String")) {
                            parameterName = "String";
                        }

                        para.add(parameterName);
                    }

                    if (return_type.equals("class java.lang.String")) {
                        return_type = "String";
                    }

                    para_map.put(methodName, new ArrayList(para));
                    return_map.put(methodName, new String(return_type));
                }
            }
        } catch (ClassNotFoundException var17) {
            var17.printStackTrace();
        }

    }

    public Map<String, ArrayList<String>> 读取类参数类型哈希表() {
        return para_map;
    }

    public Map<String, String> 读取类返回类型哈希表() {
        return return_map;
    }

    class Book {
        private String id;
        private String name;
        private String author;

        Book() {
        }

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAuthor() {
            return this.author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }
    }
}
