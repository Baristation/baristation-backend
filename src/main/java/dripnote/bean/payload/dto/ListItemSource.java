package dripnote.bean.payload.dto;// 동일 패키지나 DTO 패키지 어딘가에 존재하는 코드
import dripnote.bean.domain.Bean;
import dripnote.bean.domain.Product;

public record ListItemSource(
        Bean bean,
        Product product
) {
}