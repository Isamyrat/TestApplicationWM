/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.owner;

import org.springframework.samples.petclinic.model.Status;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import java.util.*;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class VisitController {

	private final VisitRepository visits;

	private final PetRepository pets;

	public VisitController(VisitRepository visits, PetRepository pets) {
		this.visits = visits;
		this.pets = pets;
	}

	@ModelAttribute("vets")
	public Collection<Vet> vetsAll() {
		return this.visits.findVets();
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("petId") int petId,  Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		pet.setVisitsInternal(this.visits.findByPetId(petId));
		model.put("pet", pet);

		Visit visit = new Visit();
		pet.addVisit(visit);

		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
	@GetMapping("/owners/*/pets/{petId}/visits/new")
	public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@Valid Visit visit,   BindingResult result) {

		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}
		else {
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/visit/{visitId}/edit")
	public String initUpdateForm(@PathVariable("visitId") int visitId, Model model) {
		Visit visit = this.visits.findById(visitId);

		model.addAttribute("visit", visit);

		return "pets/createOrUpdateVisitForm";
	}

	@PostMapping("/owners/{ownerId}/pets/{petId}/visit/{visitId}/edit")
	public String processUpdateForm(@Valid Visit visit, BindingResult result, ModelMap model,@PathVariable("visitId") int visitId) {
		if (result.hasErrors()) {
			model.put("visit", visit);
			return "pets/createOrUpdateVisitForm";
		}
		else {
			visit.setId(visitId);
			this.visits.save(visit);
			return "redirect:/owners/{ownerId}";
		}
	}
	@PostMapping("/owners/{ownerId}/pets/{petId}/visit/{visitId}/cancel")
	public String cancelVisit(@PathVariable("visitId") int visitId) {
			Visit visit1 = visits.findById(visitId);
			if(visit1 != null){
				visit1.setStatus(Status.CANCELLED);
			this.visits.save(visit1);
			}
			return "redirect:/owners/{ownerId}";
	}
}
