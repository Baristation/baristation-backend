package baristation.bean.payload.dto;// 동일 패키지나 DTO 패키지 어딘가에 존재하는 코드
import baristation.bean.domain.Bean;
import baristation.bean.domain.Product;

public record ListItemSource(
        Bean bean,
        Product product
) {
}