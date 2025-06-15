package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entites.Contact;
import com.smart.entites.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;

	// method for adding common data to response
	@ModelAttribute
	public void addCommanData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("USERNAME " + userName);

		// get the user usingn username(Email)

		User user = userRepository.getUserByuserName(userName);
		System.out.println("USER " + user);

		model.addAttribute("user", user);

	}

	// dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model, Principal principal) {

		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}

	// open ass form controller

	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());

		return "normal/add_contact_form";
	}

	// process add contact form
	@PostMapping("process-contact")
	public String processContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Principal principal, HttpSession session) {

		try {
			String name = principal.getName();

			User user = this.userRepository.getUserByuserName(name);

			// processing and uploading file...

			if (file.isEmpty()) {
				// if the file is empty then try our message
				System.out.println("file is empty");
				contact.setImage("contact.png");

			} else {
				// file the file folder and update the name to contact
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				System.out.println("Image is upload");

			}

			user.getContacts().add(contact);

			contact.setUser(user);

			this.userRepository.save(user);

			System.out.println("DATA " + contact);

			System.out.println("added to data base");

			// message success...
			session.setAttribute("message", new Message("You contact is added !! Add more...", "success"));

		} catch (Exception e) {
			System.out.println("ERROR " + e.getMessage());
			e.printStackTrace();
			// message error
			session.setAttribute("message", new Message("Some went wrong !! Try agian", "danger"));
		}

		return "normal/add_contact_form";
	}

	// show contact
	// per page=5[n]
	// current page =0 [page]

	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page, Model model, Principal principal) {

		model.addAttribute("title", "Show User Contact");

		String userName = principal.getName();

		User user = this.userRepository.getUserByuserName(userName);

		// current page
		// contact per page
		Pageable pageable = PageRequest.of(page, 5);

		Page<Contact> contacts = this.contactRepository.findContactsByUser(user.getId(), pageable);

		model.addAttribute("contacts", contacts);
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", contacts.getTotalPages());

		return "normal/show_contacts";
	}

	// show particular contact details

	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId, Model model, Principal principal) {

		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		String userName = principal.getName();

		User user = this.userRepository.getUserByuserName(userName);

		if (user.getId() == contact.getUser().getId()) {
			model.addAttribute("contact", contact);
			// model.addAttribute("title", contact.getName());
			model.addAttribute("title", "Show Contact");
		}

		return "normal/contact_detail";
	}

	// delete contactt handler

	@GetMapping("/delete/{cid}")
	@Transactional
	public String deleteContact(@PathVariable("cid") Integer cId, Model model, HttpSession session,
			Principal principal) {

		Contact contact = this.contactRepository.findById(cId).get();

		// check

		User user = this.userRepository.getUserByuserName(principal.getName());

		user.getContacts().remove(contact);

		this.userRepository.save(user);

		session.setAttribute("message", new Message("Contact Delete successfully...", "success"));

		return "redirect:/user/show-contacts/0";
	}

	// open update from handler

	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cId, Model model) {

		model.addAttribute("title", "Update Contact");

		Contact contact = this.contactRepository.findById(cId).get();

		model.addAttribute("contact", contact);

		return "normal/update_form";
	}

	// update contact handler
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file,
			Model m, HttpSession session, Principal principal) {

		try {
			// old contact details

			Contact oldContactDetails = this.contactRepository.findById(contact.getcId()).get();
			// image
			if (!file.isEmpty()) {

				// delete photo

				File deleteFile = new ClassPathResource("static/img").getFile();
				File file1 = new File(deleteFile, oldContactDetails.getImage());
				file1.delete();
				// update photo
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				contact.setImage(file.getOriginalFilename());

			} else {
				contact.setImage(oldContactDetails.getImage());
			}

			User user = this.userRepository.getUserByuserName(principal.getName());

			contact.setUser(user);

			this.contactRepository.save(contact);

			session.setAttribute("message", new Message("Your contact is updated....", "success"));

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

		return "redirect:/user/" + contact.getcId() + "/contact";
	}

	// your profile handler

	@GetMapping("/profile")
	public String yourProfile(Model model) {

		model.addAttribute("title", "Profile Page");

		return "normal/profile";
	}

	// open setting handler
	@GetMapping("/settings")
	public String openSettings() {

		return "normal/settings";
	}

	// change password handler
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

		System.out.println("OLD PASSWORD " + oldPassword);
		System.out.println("NEW PASSWORD " + newPassword);

		String userName = principal.getName();
		User currentUser = this.userRepository.getUserByuserName(userName);

		if (this.bCryptPasswordEncoder.matches(oldPassword, currentUser.getPassword())) {
			// change the password

			currentUser.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is successfully changed...", "success"));

		} else {
			// error....
			session.setAttribute("message", new Message("Please Enter correct old password !!", "danger"));
			return "redirect:/user/settings";
		}

		return "redirect:/user/index";

	}

	// creating order for payment

	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data) throws RazorpayException {
		System.out.println(data);

		int amt = Integer.parseInt(data.get("amount").toString());

		
			RazorpayClient client = new RazorpayClient("rzp_test_g7uMVHBCxNGxcK", "3B7pndhzx57udnEFfBQAIwQ6");
			  JSONObject ob = new JSONObject();
			  ob.put("amount", amt*100); // amount in the smallest currency unit
			  ob.put("currency", "INR");
			  ob.put("receipt", "txn_11");
			  
			  //create new order

			  Order order = client.Orders.create(ob);
			  System.out.println(order);
			  
		
		
		
		return order.toString();

	}

}
