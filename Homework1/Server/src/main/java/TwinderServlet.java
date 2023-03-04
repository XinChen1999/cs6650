import com.google.gson.Gson;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

@WebServlet(name = "TwinderServlet", value = "/TwinderServlet")
public class TwinderServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        String urlPath = req.getPathInfo();

        // check we have a URL!
        if (urlPath == null || urlPath.isEmpty()) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            res.getWriter().write("invalid url");
            return;
        }

        String[] urlParts = urlPath.split("/");
        // and now validate url path and return the response status code
        // (and maybe also some value if input is valid)

        if (!validateAction(urlParts)) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            // process the request body
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = req.getReader();
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    sb.append(line).append('\n');
                }
            } finally {
                reader.close();
            }
            String json = sb.toString();
//            System.out.println(json);
            res.setStatus(HttpServletResponse.SC_CREATED);
        }
    }

    private boolean validateAction(String[] urlPath) {
        // urlPath  = "/swipe/left"
        // urlParts = [, left/right]
        if (urlPath[0].length() != 0){
            return false;
        }
        return Objects.equals(urlPath[1], "left") || Objects.equals(urlPath[1], "right");
    }
}
