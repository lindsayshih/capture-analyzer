package capanalyzer.model;

import java.util.Date;
import java.util.Random;

public class Message extends ModelObject {
	private CaptureDbTable captureDbTable;
	private int id;
	private String subject;
	private String from;
	private String date;
	private boolean isSpam;
	private String body;
	public Message(String subject, String from, String date,String body) {
		this.subject=subject;
		this.from=from;
		this.date=date;
		this.body=body;
	}

	/* default*/ void setCaptureDbTable(CaptureDbTable captureDbTable) {
		firePropertyChange("captureDbTable", this.captureDbTable, this.captureDbTable = captureDbTable);
	}

	public CaptureDbTable getCaptureDbTable() {
		return captureDbTable;
	}

	public String toString() {
		return subject;
	}

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getSubject() {
		return subject;
	}

	public String getFrom() {
		return from;
	}

	public String getDate() {
		return date;
	}

	public void setSpam(boolean isSpam) {
		firePropertyChange("spam", this.isSpam, this.isSpam = isSpam);
	}

	public boolean isSpam() {
		return isSpam;
	}

	public String getBody() {
		return body;
	}

	public static Message createExample(int i) {
		if (i==-1) {
			i = new Random().nextInt(6);
		}
		String from;
		String body;
		String subject;
		boolean spam = false;
		switch (i) {
		case 5:
			from="\"eBay Cash Machine\" <connectso@atechonline.net>";
			spam = true;
			subject="[Spam] Quit Your Day Job Within 30 Days";
			body="Ever heard of the eBay ca$h machine ?\r\n" + 
					"\r\n" + 
					"\"How would you like an extra $250 - $1000 a week on eBay with 15 minutes of\r\n" + 
					"your time ?\"\r\n" + 
					"\r\n" + 
					" I've made it my job to help people succeed online.\r\n" + 
					"I'm constantly on the lookout for the best ways and means to make your job\r\n" + 
					"simpler, and I pass the good stuff on to you.\r\n" + 
					"\r\n" + 
					"I have developed the eBay Cash Machine - it allows everyone to make a great\r\n" + 
					"income on eBay 99% automatically. It only takes a few minutes to set up and\r\n" + 
					"once that is done you will have your own eBay Businesses that literally run\r\n" + 
					"on auto-pilot!\r\n" + 
					"You just wait for the money to come in!\r\n" + 
					"\r\n" + 
					"I have found a foolproof method how anyone can easily make an extra $250,\r\n" + 
					"$500 or even $1000 per week on eBay using my eBay Ca$h Machine.\r\n" + 
					"\r\n" + 
					"Now before I get to deep into the details let me tell you what the eBay\r\n" + 
					"Ca$h Machine ISN'T.\r\n" + 
					"\r\n" + 
					"It is NOT a promote my affiliate program to make m.oney!\r\n" + 
					"It is NOT a NOT A Get-Rich Q.uick Scheme!\r\n" + 
					"It is NOT a Face To Face Selling program of Any Kind!\r\n" + 
					"It is NOT a Trial Offer to sign up for!\r\n" + 
					"It is NOT some garbage product that refers you to purchase other garbage\r\n" + 
					"products.\r\n" + 
					"It is NOT a M.LM, a Pyramid Scheme, Gifting or anything else like that.\r\n" + 
					"It is NOT like anything you have ever seen.\r\n" + 
					"\r\n" + 
					"For Full Details please read the attached .html file\r\n" + 
					"\r\n" + 
					"To Unsubscribe please read the attached Unsubscribe.txt";
			break;
		case 4:
			from="\"Matthew Casey\" <kefxeroxuadiz@xeroxua.com>";
			subject="Through strong partnerships, bulk purchases, and special promotions, we have teamed up with some of the top distributors of top-selling, brand name products to give you the best selection of products available on the Internet.";
			body="We sell them for a fraction of a price..\r\n" + 
					"You'll have all the class, and still have all your money.\r\n" + 
					"Rep|_icated to the Smallest Detail\r\n" + 
					"\r\n" + 
					"http://vvfpk.cordjump.com/?ctnp";
			break;
		case 3:
			from="\"Robbie Hogue\" <kefxenondiz@xenon.md>";
			subject="We sincerely hope that our website will put you on the right track in finding your ideal rep!_ica watch";
			body="Show your love and gratitude to your nearest and dearest people with the best quality gifts available on the internet!\r\n" + 
					"Free shipping if you order 5 or more\r\n" + 
					"By the way We sell only quality goods.\r\n" + 
					"\r\n" + 
					"http://khncq.covecusp.com/?cnc";
			break;
		case 2:
			from = "\"Nichole Kerr\" <kefxinfuerdiz@xinfuer.com>";
			subject = "Be leaner and slimmer by next week!";
			body = "Weight loss supplement\r\n" + 
					"Watch your body change with Vital Acai\r\n" + 
					"Don't hate the scales - start loving them.\r\n" + 
					"\r\n" + 
					"http://x.listsoon.com/?tve\r\n" + 
					"";
			break;
		case 1:
			from = "\"Jose Dunn\" <tue@bossphilly.com>";
			subject = "The cheerful and kind girl searches for the beloved";
			body = "Hello!\r\n" + 
					" My name is Tatyana to me of  years Hope I\r\n" + 
					" will be lucky :-)  \r\n" + 
					"I don't think that the age and appearance is so important though I am \r\n" + 
					"rather pretty. The most important what is inside you and how do you \r\n" + 
					"feel about the life. I know this life from many sides and I am rather \r\n" + 
					"mature already to know how to make a man happy.\r\n" + 
					"I don't know if you answer me or not. But why not to try? I will regret\r\n" + 
					" \r\n" + 
					"if not to try. I think we should use every chance to find our\r\n" + 
					" happiness. \r\n" + 
					"Life is too short to use it only for thinking and dreaming. I try to\r\n" + 
					" act \r\n" + 
					"but not only to dream.\r\n" + 
					"So here I am :-)\r\n" + 
					"I will not write you much about myself now. I will just give you an\r\n" + 
					" idea \r\n" + 
					"of who I am. \r\n" + 
					"I work to earn for my living. I have a higher education and I am rather\r\n" + 
					" \r\n" + 
					"intelligent. The only one I miss is a beloved person and I want to have\r\n" + 
					" a \r\n" + 
					"family. I have really many interests: music, reading, books, computers,\r\n" + 
					" \r\n" + 
					"movies, good conversations, sports and many other things which make \r\n" + 
					"the life wonderful. I like beautiful clothes and things. I can't say\r\n" + 
					" that I \r\n" + 
					"have many friends. I know many people but I am very picky about the \r\n" + 
					"friends. Friend is a person who will be with me all my life. And I am \r\n" + 
					"lucky to have some really good friends. But I am very sociable that is \r\n" + 
					"why there are many people I have good relations with. \r\n" + 
					"I live with my mum and she is very friendly though don't understand \r\n" + 
					"each other always. I am rather independent. And work to be able to pay \r\n" + 
					"for the things I want to have in my life. In some words I can tell you,\r\n" + 
					" \r\n" + 
					"that I enjoy the life as it is and I love the life with all its\r\n" + 
					" aspects. I am \r\n" + 
					"very kind (I am not boasting :-)) which hurts me often. But I am strong\r\n" + 
					" \r\n" + 
					"enough to overcome the hardships on my way. \r\n" + 
					"Well, now it is up to you to decide to write me or not. \r\n" + 
					"\r\n" + 
					"Please write to me directly at:   mailtaty@gmail.com\r\n" + 
					"\r\n" + 
					"I must to tell you that I am unable to use mail box of this site\r\n" + 
					" because \r\n" + 
					"I used services of the Internet cafe.\r\n" + 
					"I still hope for your reply. Have a good day!!!!!!! \r\n" + 
					"Tatyana";
			break;
		case 0:
			from="\"Alana Moyer\" <kefxcamdiz@xcam.at>";
			spam = true;
			subject="[Spam] Finally get the attention you deserve! ";
			body="More and more women are learning that Ultra Curves is the product they need to help them get the attention they deserve.\r\n" + 
					" Don't get left behind!\r\n" + 
					" Take advantage and improve yourself along with millions of other women today!\r\n" + 
					"\r\n" + 
					"http://www.jzhpoi.cn/?izjogiwtuz";
			break;
		default:
			from = "nobody@nowhere.org";
			subject = "(no subject)";
			body = "empty";
			break;
		}
		Message message = new Message(subject, from, new Date().toString(), body);
		message.setSpam(spam);
		return message;
	}
}
