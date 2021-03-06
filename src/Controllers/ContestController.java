package Controllers;

import java.io.IOException;
import java.util.List;
import javax.jms.Session;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.text.AbstractDocument.Content;
import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import util.Compiler;

import Dao.CodeDao;
import Dao.PaperDao;
import Dao.UserDao;
import Models.Code;
import Models.Paper;
import Models.User;

/**************************
* 说明：    竞赛控制器
***************************
* 类名：    ContestController
* 包名：    Controllers
***************************/
@Controller
public class ContestController {
	@Autowired
	private UserDao userDao;
	@Autowired
	private PaperDao paperDao;
	@Autowired
	private CodeDao codeDao;
	
	private static List<Paper> mList;
	
	
	/**************************************************
	 * 限定符：	公开
	 * 说明：	跳转竞赛页面
	 * 方法名：	index
	 **************************************************
	 * 参数表：
	 * @return 	String	跳转到contest.jsp
	 **************************************************/
	@RequestMapping(value="/contest")
	public String index(HttpServletRequest request) {
		return "contest";
	}
	
	
	/**************************************************
	 * 限定符：	公开
	 * 说明：	跳转答题页面
	 * 方法名：	answer
	 **************************************************
	 * 参数表：
	 * @param 	request
	 * @return 	String		跳转到answer.jsp
	 **************************************************/
	@RequestMapping(value="/answer")
	public String answer(HttpServletRequest request) {
		if (mList == null) {
			mList = paperDao.getAllPaper();
		}
		request.setAttribute("list", mList);
		return "answer";
	}
	
	
	/**************************************************
	 * 限定符：	公开
	 * 说明：	结算成绩更新用户信息并跳转结果页面
	 * 方法名：	settlement
	 **************************************************
	 * 参数表：
	 * @param 	request		通过链接/settlement?correct=
	 * @param 	correct		正确答案数量
	 * @return 	String		跳转settlement.jsp
	 **************************************************/
	@RequestMapping(value="/settlement1")
	public String settlement(HttpServletRequest request, int correct) {
		int wrong = 10 - correct;
		User user = (User) request.getSession().getAttribute("user");
		user.setWeekly_count(user.getWeekly_count() + 1);
		request.setAttribute("correct", correct);
		request.setAttribute("rank", "none");
		if (correct >= 5) {
			user.setWeekly_win(user.getWeekly_win() +1);
			request.setAttribute("score", "victory");
			int points = user.getPoints() + correct - wrong;
			if (points >= 100) {
				user.setPoints(0 + points);
				user.setRank(user.getRank() + 1);
				request.setAttribute("rank", "up");
			}else {
				user.setPoints(user.getPoints() + correct);
			}
		}else {
			request.setAttribute("score", "defeat");
			int points = user.getPoints() + correct - wrong;
			if (points <= 0) {
				user.setPoints(100 + points);
				user.setRank(user.getRank() - 1);
				request.setAttribute("rank", "down");
			}else {
				user.setPoints(user.getPoints() + points);
			}
		}
		userDao.updateUser(user);
		return "settlement";
	}
	
	
	@RequestMapping(value="/ajaxContentByItem")
	public void ajaxContentByItem(HttpServletRequest request,HttpServletResponse response) throws IOException {
		if (mList == null) {
			mList = paperDao.getAllPaper();
		}
		int item = Integer.valueOf(request.getParameter("item"));
		String title = mList.get(item).getTitle();
		String content = mList.get(item).getContents();
		String trips = "";
		switch (item) {
			case 0:
			trips = "给定输入：[\"babad\"]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[\"bab\"] or [\"aba\"]";
			break;
			case 1:
				trips = "给定输入：[1,4,6,7]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[4,6,7,1]";
				break;
			case 2:
				trips = "给定输入：[2,3,-2,4]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[6]";
				break;
			case 3:
				trips = "给定输入：[无]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[Trie三种操作]";
				break;
			case 4:
				trips = "给定输入：[2,3,4]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[3]";
				break;
			case 5:
				trips = "给定输入：[[X,X,X,X][X,O,O,X][X,X,O,X][X,X,X,X]]";
				break;
			case 6:
				trips = "给定输入：[cache.put(2, 2)]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[1]";
				break;
			case 7:
				trips = "给定输入：[[1,1],2,[1,1]]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[1,1,2,1,1]";
				break;
			case 8:
				trips = "给定输入：[[2,10],[3,15],[7,12]]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[12,0]";
				break;
			case 9:
				trips = "给定输入：[\"aab\"]&nbsp;&nbsp;&nbsp;&nbsp;要求输出：[[\"aa\",\"b\"],[\"a\",\"a\",\"b\"]]";
				break;
		default:
			break;
		}
		
		
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().print(Integer.toString(item + 1) + " . " + title + "$$" + content + "$$" + trips);
	}
	
	@RequestMapping(value="/ajaxRunCode")
	public void ajaxRunCode(HttpServletRequest request,HttpServletResponse response) throws IOException {
		System.out.println("转义前：" + request.getParameter("code"));
		String interpretId = Compiler.run(request.getParameter("lang"), request.getParameter("code"));
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().print(interpretId);
	}
	
	@RequestMapping(value="/ajaxCheckCode")
	public void ajaxCheckCode(HttpServletRequest request,HttpServletResponse response) throws IOException {
		String result = Compiler.get(request.getParameter("interpretId"));
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().print(result);
	}
	
	@RequestMapping(value="/ajaxChangeCode")
	public void ajaxChangeCode(HttpServletRequest request,HttpServletResponse response) throws IOException {
		Code code = codeDao.getAllCodeByName(request.getParameter("lang"));
		response.setContentType("text/html;charset=utf-8");
		response.getWriter().print(code.getCode());
	}
	
	@RequestMapping(value="/settlement")
	public String goSettlement() {
		return "settlement";
	}
}
