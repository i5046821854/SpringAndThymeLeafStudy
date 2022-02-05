package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;


@Slf4j
@Controller
@RequestMapping("/validation/v3/items")
@RequiredArgsConstructor
public class ValidationItemControllerV3 {

    private final ItemRepository itemRepository;

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v3/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v3/addForm";
    }
    @PostMapping("/add")
    public String addItem(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {  //@validated가 있어서 자동으로 bean 검증 (item에 annotatiom을 기반으로, 스프링 부트가 자동으로 글로벌 validator로 LocalValidatorFactoryBean을 등록해줌, but, 바인딩 성공한 필드만) / spring boot starter validation 라이브러리를 넣은 후

       // itemValidator.validate(item, bindingResult);   //검증 로직 분리
        // @initBinder에서 검증을 @Validated에 대한 검증을 진행해줘서 bindingResult에 담음

        if (item.getPrice() != null && item.getQuantity() != null){   //object에러를 애노테이션으로 하는 것은 취약점이 많음. 얘는 자바 코드로 구현하는 것이 나을듯!
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000)
            {
                bindingResult.reject("totalPriceMin", "가격 * 수량의 합은 10000 이상이어야함. 현재 값 = " + resultPrice);
            }
        }

        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v3/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v3/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v3/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @Validated @ModelAttribute Item item, BindingResult bindingResult) {

        if (item.getPrice() != null && item.getQuantity() != null){   //object에러를 애노테이션으로 하는 것은 취약점이 많음. 얘는 자바 코드로 구현하는 것이 나을듯!
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000)
            {
                bindingResult.reject("totalPriceMin", "가격 * 수량의 합은 10000 이상이어야함. 현재 값 = " + resultPrice);
            }
        }

        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v3/editForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }


        itemRepository.update(itemId, item);
        return "redirect:/validation/v3/items/{itemId}";
    }

}

