package com.example.bankapp.controller;

import com.example.bankapp.dto.TransferRequest;
import com.example.bankapp.model.Customer;
import com.example.bankapp.service.BankService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class BankController {

    private final BankService bankService;

    @GetMapping("/")
    public String dashboard(@RequestParam(required = false) String keyword, Model model) {
        model.addAttribute("customers", bankService.getAllCustomers(keyword));
        model.addAttribute("totalBalance", bankService.getTotalBalance());
        model.addAttribute("latestTransactions", bankService.getLatestTransactions());

        // Populate form backing objects expected by bank.html
        if (!model.containsAttribute("transferRequest")) {
            model.addAttribute("transferRequest", new TransferRequest());
        }
        if (!model.containsAttribute("customer")) {
            model.addAttribute("customer", new Customer());
        }

        model.addAttribute("keyword", keyword);
        return "bank";
    }

    @PostMapping("/customers")
    public String addCustomer(@Valid @ModelAttribute("customer") Customer customer,
                              BindingResult bindingResult,
                              RedirectAttributes redirectAttributes,
                              Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", bankService.getAllCustomers(null));
            model.addAttribute("totalBalance", bankService.getTotalBalance());
            model.addAttribute("latestTransactions", bankService.getLatestTransactions());
            model.addAttribute("transferRequest", new TransferRequest());
            return "bank";
        }
        try {
            bankService.createCustomer(customer);
            redirectAttributes.addFlashAttribute("successMessage", "Customer created successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add customer: " + ex.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/transfer")
    public String handleTransfer(@Valid @ModelAttribute("transferRequest") TransferRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("customers", bankService.getAllCustomers(null));
            model.addAttribute("totalBalance", bankService.getTotalBalance());
            model.addAttribute("latestTransactions", bankService.getLatestTransactions());
            model.addAttribute("customer", new Customer());
            return "bank";
        }
        try {
            bankService.transfer(request.getFromAccount(), request.getToAccount(), request.getAmount(), request.isSimulateFailure());
            redirectAttributes.addFlashAttribute("successMessage", "Transfer completed successfully!");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Transfer Failed: " + ex.getMessage() + ". All changes were rolled back.");
        }
        return "redirect:/";
    }

    @GetMapping("/accounts/{accountNumber}/history")
    public String viewHistory(@PathVariable String accountNumber, Model model) {
        model.addAttribute("accountNumber", accountNumber);
        model.addAttribute("history", bankService.getAccountHistory(accountNumber));
        model.addAttribute("totalSent", bankService.getTotalSent(accountNumber));
        return "history";
    }

    @GetMapping("/large-transfers")
    public String largeTransfers(@RequestParam(defaultValue = "10000") BigDecimal min, Model model) {
        model.addAttribute("minAmount", min);
        model.addAttribute("transactions", bankService.getLargeTransfers(min));
        return "large-transfers";
    }
}