package mvc.command;

import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;
import com.util.ConnectionProvider;

import ohora.domain.ProductDTO;
import ohora.persistence.OhoraDAO;
import ohora.persistence.OhoraDAOImpl;

public class ViewHandler implements CommandHandler {

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) throws Exception {
        System.out.println("View Handler process...");

        // 상품 ID를 요청 파라미터에서 가져옴
        String productId = request.getParameter("product_id");
        System.out.println("Received product_id: " + productId);  // 디버깅용 출력

        // DAO 및 데이터베이스 연결
        Connection conn = null;
        OhoraDAO dao = null;
        ProductDTO product = null;

        try {
            conn = ConnectionProvider.getConnection();
            dao = new OhoraDAOImpl(conn);
            
            // 상품 ID가 null이 아니고 유효할 때 상품 조회
            if (productId != null && !productId.trim().isEmpty()) {
                int pdtId = Integer.parseInt(productId);
                product = dao.selectProductById(pdtId);  // 상품 ID로 특정 상품 정보 조회
                
                System.out.println("DB에서 조회한 상품 정보: " + product);
            } else {
                System.out.println("상품 ID가 유효하지 않습니다.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (conn != null) conn.close();
        }

        // 상품 정보를 요청에 저장
        request.setAttribute("product", product);
        
        //AJAX 요청인지 확인
        String requestedWith = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equals(requestedWith);
        
        if (isAjax) {
			//AJAX 요청인 경우 JSON으로 응답
        	response.setContentType("application/json");
        	response.setCharacterEncoding("UTF-8");
        	
        	//Gson을 사용해 ProductDTO 객체를 JSON형식으로 변환
        	Gson gson = new Gson();
        	String jsonResponse = gson.toJson(product);
        	
        	//JSON 데이터르 클라이언트에 전송
        	PrintWriter out = response.getWriter();
        	out.print(jsonResponse);
        	out.flush();
        	
        	// AJAX 요청이므로 JSP로 이동하지 않음
            return null;
        } else {
        	// 일반 요청인 경우 JSP로 포워딩
            return "/ohora/prd_detail_view.jsp";
		}
        
    }
}
