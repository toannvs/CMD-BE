package com.comaymanagement.cmd.controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.comaymanagement.cmd.service.DepartmentService;
import com.comaymanagement.cmd.service.EmployeeService;
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/employees")
public class EmployeesController {
	private final Logger LOGGER = LoggerFactory.getLogger(this.getClass());
	@Autowired
	EmployeeService employeeService;
	@Autowired
	DepartmentService departmentService;
	
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal)")
	@PostMapping(value = "", produces = "application/json")
	public ResponseEntity<Object> paggingAllEmployee(
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestBody String json) {
		ResponseEntity<Object> result = employeeService.employeePaging(page, sort, order, json);
		return result;
	}
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal)")
	@PostMapping(value = "/teams", produces = "application/json")
	public ResponseEntity<Object> paggingAllEmployeeTeam(
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestBody String json) {
		ResponseEntity<Object> result = employeeService.employeePagingTeams(page, sort, order, json);
		return result;
	}
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal)")
	@PostMapping(value = "/download", produces = "application/json")
	public ResponseEntity<Object> findAllWithParamAndNotLimit(
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestBody String json) {
		ResponseEntity<Object> result = employeeService.findAllWithParamAndNotLimit(page, sort, order, json);
		return result;
	}
	
	@PreAuthorize("@customRoleService.canCreate('employee',principal)")
	@PostMapping(value = "/add")
	@ResponseBody
	public ResponseEntity<Object> addEmployee(@RequestBody String json) {
		return employeeService.addEmployee(json);
	}

//	@PreAuthorize("@customRoleService.canUpdate('employee',principal) ")
	@PutMapping(value = "/edit")
	@ResponseBody
	public ResponseEntity<Object> editEmployee(@RequestBody String json) {
		return employeeService.edit(json);
	}

	@PreAuthorize("@customRoleService.canDelete('employee',principal)")
	@DeleteMapping(value = "/delete/{id}")
	@ResponseBody
	public ResponseEntity<Object> deleteEmployee(@PathVariable Integer id) {
		return employeeService.delete(id);
	}
	@PreAuthorize("@customRoleService.canView('employee',principal)")
	@GetMapping(value = "/name")
	@ResponseBody
	public ResponseEntity<Object> findByName(
			@RequestParam(value = "name", required = false) String name) {
		return employeeService.findByName(name);
	}
	@PreAuthorize("@customRoleService.canView('employee',principal)")
	@GetMapping(value = "/{id}")
	@ResponseBody
	public ResponseEntity<Object> findByName(
			@PathVariable(value = "id", required = false) Integer id) {
		return employeeService.findById(id);
	}

	// Example return ResponseEntity

//	@GetMapping("/{id}")
//	public ResponseEntity<Object> findById(@PathVariable Long id) {
//		Optional<Produce> produce = produceRepository.findById(id);
//		
//		if (produce.isPresent()) {
//			return ResponseEntity.status(HttpStatus.OK).body(
//					new ResponseObject("OK","Query produce successfully: ", produce)
//			);
//		}else {
//			
//			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
//				new ResponseObject("Not found","Produce not found with id: " + id,"")	
//			);
//		}
//		
//	}

//	@PutMapping("/update/{id}")
//	public ResponseEntity<ResponseObject> updateProduce(@RequestBody Produce produceUpdate,@PathVariable Long id) {
//		Optional<Produce>  updateProduce = produceRepository.findById(id);
//		if(updateProduce.isPresent()) {
//			updateProduce.map(produce -> {
//			
//					produce.setProduceName(produceUpdate.getProduceName());
//					produce.setYear(produceUpdate.getYear());
//					produce.setPrice(produceUpdate.getPrice());
//					return produceRepository.save(produce);
//
//				});
//
//			return ResponseEntity.status(HttpStatus.OK).body(
//					new ResponseObject("OK","Edited!", "")
//					);
//		}else {
//			return ResponseEntity.status(HttpStatus.OK).body(
//					new ResponseObject("Not Exists","Produce not exists", "")
//					);
//		}
//		
//					
//		
//	}
	
	@PreAuthorize("@customRoleService.canImport('employee',principal)")
	@PostMapping("/import")
	public ResponseEntity<Object> uploadFile(@RequestParam("file") MultipartFile multipartFile) {
		return employeeService.importEmployees(multipartFile);
	}
	
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal)")
	@GetMapping(value = "/notifies", produces = "application/json")
	public ResponseEntity<Object> findAllNotifies(
			@RequestParam(value = "page", required = false) String page,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "order", required = false) String order,
			@RequestParam(value = "search", required = false) String keySearch) {
		ResponseEntity<Object> result = employeeService.findAllNotifies(page, sort, order, keySearch);
		return result;
	}
	
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal)")
	@PostMapping(value = "/notifies/allRead", produces = "application/json")
	public ResponseEntity<Object> allReadNotifies(@RequestBody String json) {
		ResponseEntity<Object> result = employeeService.markIsReadNotifies(json);
		return result;
	}
	
	@PreAuthorize("@customRoleService.canView('employee',principal) or @customRoleService.canViewAll('employee', principal)")
	@DeleteMapping(value = "/notifies", produces = "application/json")
	public ResponseEntity<Object> deleteNotifies(@RequestBody String json) {
		ResponseEntity<Object> result = employeeService.deleteNotifies(json);
		return result;
	}
	
}
