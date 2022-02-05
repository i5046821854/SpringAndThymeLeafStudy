package hello.itemservice.web.validation;

import hello.itemservice.domain.item.Item;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class ItemValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Item.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {  //Error는 BindingResult의 부모 클래스
        Item item = (Item) target;
        if(!StringUtils.hasText(item.getItemName())){
//            bindingResult.addError(new FieldError("item", "itemName", item.getItemName(), false, new String[]{"required.item.itemName"}, null, null));   //properties를 참조하여 에러 메시지를 띄우도록

            errors.rejectValue("itemName", "required", null);   //bindingResult는  target 객체를 이미 알고 있으므로 objectName을 생략한 rejectValue 메소드로 사용 가능
        }
        if (item.getPrice() == null || item.getPrice() < 1000 || item.getPrice() > 1000000){
            //           bindingResult.addError(new FieldError("item","price",item.getPrice(), false ,new String[]{"range.item.price"}, new Object[]{1000,1000000}, null));
            errors.rejectValue("price", "range",new Object[]{1000,1000000}, null);
        }
        if (item.getQuantity() == null || item.getQuantity() >= 9999){
            //         bindingResult.addError(new FieldError("item", "quantity",item.getQuantity(), false ,new String[]{"max.item.quantity"}, new Object[]{9999}, null));
            errors.rejectValue("quantity", "max", new Object[]{9999},null);
        }

        if (item.getPrice() != null && item.getQuantity() != null) {
            int resultPrice = item.getPrice() * item.getQuantity();
            if (resultPrice < 10000) {
                //           bindingResult.addError(new ObjectError("item",new String[]{"totalPriceMin"}, new Object[]{10000, resultPrice}, null)); // 필드에 관한 에러가 아니므로 object error
                errors.reject("totalPriceMin", new Object[]{10000, resultPrice}, null);
            }
        }

    }
}
