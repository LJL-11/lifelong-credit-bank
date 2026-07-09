# Java程序设计基础

## 课程概述
Java 是目前最流行的面向对象编程语言之一，广泛应用于企业级开发、Android 应用开发、大数据处理等领域。本课程从零基础开始，系统讲解 Java 语言的核心语法和编程思想。

## 第一章：Java 语言基础

### Java 的发展历史
Java 由 Sun Microsystems 公司于 1995 年发布，创始人为 James Gosling。2010 年被 Oracle 收购，目前最新 LTS 版本为 Java 21。

### Java 的特点
- **跨平台性**：一次编写，到处运行（Write Once, Run Anywhere）。通过 JVM 实现平台无关性。
- **面向对象**：Java 是纯面向对象语言，支持封装、继承、多态三大特性。
- **自动内存管理**：JVM 负责垃圾回收（GC），程序员无需手动释放内存。
- **强类型语言**：变量必须先声明类型才能使用，编译时检查类型安全。

### JDK、JRE、JVM 的关系
- **JVM**（Java Virtual Machine）：执行字节码的虚拟机，是跨平台的基础
- **JRE**（Java Runtime Environment）：JVM + 核心类库，用于运行 Java 程序
- **JDK**（Java Development Kit）：JRE + 开发工具（javac编译器、javadoc文档生成器等）

## 第二章：面向对象编程

### 封装
将数据和操作数据的方法绑定在一起，通过访问修饰符（private、protected、public）控制外部访问权限。封装的目的是保护数据不被随意修改，提高代码安全性。

### 继承
子类继承父类的属性和方法，使用 extends 关键字。Java 只支持单继承，但可以通过接口实现多继承的效果。继承提高了代码复用性。

### 多态
同一个方法调用，作用于不同的对象，可以有不同的解释，产生不同的执行结果。多态的实现方式：
- 方法重写（Override）
- 方法重载（Overload）
- 接口回调

## 第三章：常用类库

### String 和 StringBuilder
- **String** 是不可变类，每次修改都会创建新对象，适合少量字符串操作
- **StringBuilder** 是可变的，线程不安全但性能好，适合频繁字符串拼接
- **StringBuffer** 是可变的，线程安全但性能较差

### 集合框架
- **List**：有序可重复，ArrayList（数组实现，查询快）、LinkedList（链表实现，增删快）
- **Set**：无序不可重复，HashSet（哈希表）、TreeSet（红黑树排序）
- **Map**：键值对，HashMap（哈希表）、TreeMap（红黑树排序键）

## 第四章：异常处理

### 异常分类
- **Checked Exception**：编译时异常，必须显式处理（try-catch 或 throws）
- **Runtime Exception**：运行时异常，可以不处理，但建议预防（如 NullPointerException）
- **Error**：系统级错误，无法处理（如 OutOfMemoryError）

### 异常处理原则
- 不要捕获异常后什么都不做（空 catch 块）
- 能处理才捕获，不能处理就向上抛出
- 使用 finally 块释放资源（JDK 7+ 可使用 try-with-resources）
