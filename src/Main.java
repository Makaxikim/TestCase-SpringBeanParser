import di.Context;
import model.Car;

public class Main {
    public static void main(String[] args) {
        Context context = new Context("config.xml");
        Car car = context.getBean("car", Car.class);
        System.out.println(car);
    }
}
