package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import hello.itemservice.domain.item.ItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping("/validation/v2/items")
@RequiredArgsConstructor
public class ValidationItemControllerV2 {

    private final ItemRepository itemRepository;
    private final ItemValidator itemValidator;

    @InitBinder   //컨트롤러가 호출될때마다 validator가 바인딩되어 검증들어감 v6  (모든 컨트롤러의 요청에도 적용시킬 수 잇는 방법도 있음. 참고)
    public void init(WebDataBinder webDataBinder){
        webDataBinder.addValidators(itemValidator);
    }

    @GetMapping
    public String items(Model model) {
        List<Item> items = itemRepository.findAll();
        model.addAttribute("items", items);
        return "validation/v2/items";
    }

    @GetMapping("/{itemId}")
    public String item(@PathVariable long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/item";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("item", new Item());
        return "validation/v2/addForm";
    }

    //@PostMapping("/add")
    public String addItemV1(@ModelAttribute Item item, BindingResult bindingResult,  RedirectAttributes redirectAttributes, Model model) {  //에러난 것에 대해 바인딩 해주는 BindingResult 객체 (이는 modelAttribute에 대한 정보를 담고 있으므로 꼭 그 다음에 위채해야함)
        Map<String, String> errors = new HashMap<>();

        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", "상품 이름은 필수입니다"));
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item","price", "가격은 1000  ~ 1000000 까지 허용"));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity", "수량은 최대 9999까지 허용"));
        }

        if (item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000)
            {
                errors.put("globalError", "가격 * 수량의 합은 10000 이상이어야함. 현재 값 = " + resultPrice);
                bindingResult.addError(new ObjectError("item", "가격 * 수량의 합은 10000 이상이어야함. 현재 값 = " + resultPrice)); // 필드에 관한 에러가 아니므로 object error
            }
        }

        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV2(@ModelAttribute Item item, BindingResult bindingResult,  RedirectAttributes redirectAttributes, Model model) {  //에러난 것에 대해 바인딩 해주는 BindingResult 객체 (이는 modelAttribute에 대한 정보를 담고 있으므로 꼭 그 다음에 위채해야함)

        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, null, null, "상품 이름은 필수입니다"));   //기존거에다가 오류 난 값을 get으로 넣어주고, binding자체는 성공했으므로(검증 오류이므로) binding failure는 false로 해줌  / 타입 오류 같은 경우에는 true로 자동적으로 설정이 되어 값이 담기게 됨
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item","price",item.getPrice(), false ,null, null, "가격은 1000  ~ 1000000 까지 허용"));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(), false ,null, null, "수량은 최대 9999까지 허용"));
        }

        if (item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000)
            {
                bindingResult.addError(new ObjectError("item",null, null, "가격 * 수량의 합은 10000 이상이어야함. 현재 값 = " + resultPrice)); // 필드에 관한 에러가 아니므로 object error
            }
        }

        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV3(@ModelAttribute Item item, BindingResult bindingResult,  RedirectAttributes redirectAttributes, Model model) {  //에러난 것에 대해 바인딩 해주는 BindingResult 객체 (이는 modelAttribute에 대한 정보를 담고 있으므로 꼭 그 다음에 위채해야함)

        if(!StringUtils.hasText(item.getItemName())){
            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));   //properties를 참조하여 에러 메시지를 띄우도록
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            bindingResult.addError(new FieldError("item","price",item.getPrice(), false ,new String[]{"range.item.price"}, new Object[]{1000,1000000}, null));
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999){
            bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(), false ,new String[]{"max.item.quantity"}, new Object[]{9999}, null));
        }

        if (item.getPrice() != null && item.getQuantity() != null){
            int resultPrice = item.getPrice() * item.getQuantity();
            if(resultPrice < 10000)
            {
                bindingResult.addError(new ObjectError("item",new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null)); // 필드에 관한 에러가 아니므로 object error
            }
        }

        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV4(@ModelAttribute Item item, BindingResult bindingResult,  RedirectAttributes redirectAttributes, Model model) {  //에러난 것에 대해 바인딩 해주는 BindingResult 객체 (이는 modelAttribute에 대한 정보를 담고 있으므로 꼭 그 다음에 위채해야함)

        if(bindingResult.hasErrors())  //타입 오류와 같은 경우는 자동적으로 bindingResult에 error로써 들어가므로, 여기서 return처리를 안해주면 밑에 rejectValue에서도 에러로 처리되어 중복
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }


        if(!StringUtils.hasText(item.getItemName())){
//            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));   //properties를 참조하여 에러 메시지를 띄우도록

            bindingResult.rejectValue("itemName", "required", null);   //bindingResult는  target 객체를 이미 알고 있으므로 objectName을 생략한 rejectValue 메소드로 사용 가능
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
 //           bindingResult.addError(new FieldError("item","price",item.getPrice(), false ,new String[]{"range.item.price"}, new Object[]{1000,1000000}, null));
            bindingResult.rejectValue("price", "range",new Object[]{1000,1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999){
   //         bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(), false ,new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            bindingResult.rejectValue("quantity", "max", new Object[]{9999},null);
        }

        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                //           bindingResult.addError(new ObjectError("item",new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null)); // 필드에 관한 에러가 아니므로 object error
                bindingResult.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }

    //@PostMapping("/add")
    public String addItemV5(@ModelAttribute Item item, BindingResult bindingResult,  RedirectAttributes redirectAttributes, Model model) {  //에러난 것에 대해 바인딩 해주는 BindingResult 객체 (이는 modelAttribute에 대한 정보를 담고 있으므로 꼭 그 다음에 위채해야함)

        if(bindingResult.hasErrors())  //타입 오류와 같은 경우는 자동적으로 bindingResult에 error로써 들어가므로, 여기서 return처리를 안해주면 밑에 rejectValue에서도 에러로 처리되어 중복
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        itemValidator.validate(item, bindingResult);   //검증 로직 분리

        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @PostMapping("/add")
    public String addItemV6(@Validated @ModelAttribute Item item, BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {  //에러난 것에 대해 바인딩 해주는 BindingResult 객체 (이는 modelAttribute에 대한 정보를 담고 있으므로 꼭 그 다음에 위채해야함) //@Validated는 해당 모델을 검증하라 이거임

       // itemValidator.validate(item, bindingResult);   //검증 로직 분리
        // @initBinder에서 검증을 @Validated에 대한 검증을 진행해줘서 bindingResult에 담음
        if(bindingResult.hasErrors())
        {
            log.info("errors = {}", bindingResult);
            return "validation/v2/addForm";   //bindingResult는 모델에 안 담고 바로 보내도 뷰에서 보임
        }

        Item savedItem = itemRepository.save(item);
        redirectAttributes.addAttribute("itemId", savedItem.getId());
        redirectAttributes.addAttribute("status", true);
        return "redirect:/validation/v2/items/{itemId}";
    }


    @GetMapping("/{itemId}/edit")
    public String editForm(@PathVariable Long itemId, Model model) {
        Item item = itemRepository.findById(itemId);
        model.addAttribute("item", item);
        return "validation/v2/editForm";
    }

    @PostMapping("/{itemId}/edit")
    public String edit(@PathVariable Long itemId, @ModelAttribute Item item) {
        itemRepository.update(itemId, item);
        return "redirect:/validation/v2/items/{itemId}";
    }

}

