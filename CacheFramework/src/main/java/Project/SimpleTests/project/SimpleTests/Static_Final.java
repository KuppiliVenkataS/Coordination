package project.SimpleTests.project.SimpleTests;

/**
 * Created by santhilata on 02/12/15.
 */
class Student{
    int id;
    String name;
    static int year=1;
    final int age= 20 ;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static int getYear() {
        return year;
    }

    public static void setYear(int year) {
        Student.year = year;
    }

    public int getAge() {
        return age;
    }



    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + ", year ="+ year+ ", age ="+age+ '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Student)) return false;

        Student student = (Student) o;

        if (id != student.id) return false;
        if (age != student.age) return false;


        return name.equals(student.name);

    }



    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + name.hashCode();
        result = 31 * result + age;
        return result;
    }
}
public class Static_Final {

    static Student s3 = new Student();

    public static void main(String[] args) {
        Student s1 = new Student();
        Student s2 = new Student();
        s1.setId(1);
        s1.setName("ABC");
       // s1.setYear(17);


        System.out.println(s1.toString());

        s2.setId(2);
        s2.setName("DEF");
        s2.setYear(17);
        System.out.println(s2.toString());

        s3.setName("FGH");
        s3.setId(3);

        if (s2.equals(s3)) System.out.println("YES");
        else System.out.println("no");
    }



}
