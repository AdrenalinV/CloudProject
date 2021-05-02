import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class main {
    public static void main(String[] args) throws SQLException {
//        Connection conn=DataSource.getConnection();
//        Statement st = conn.createStatement();
////        st.executeUpdate("DELETE FROM CLOUD.data WHERE id>0 and id<7");
//        ResultSet rs = st.executeQuery("SELECT * FROM Cloud.data");
//        while(rs.next()){
//            System.out.println(rs.getString(3));
//        }
//        rs.close();
//        st.close();
//        conn.close();
        Thread myThready = new Thread(new Runnable()
        {
            public void run() //Этот метод будет выполняться в побочном потоке
            {
                System.out.println("Привет из побочного потока!");
            }
        });
        myThready.start();	//Запуск потока

        System.out.println("Главный поток завершён...");
    }


    }

