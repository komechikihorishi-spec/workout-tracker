package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	 // 「textの中にkeywordが含まれているものを、IDの降順（最新順）で探せ」という魔法のメソッド
    List<Message> findByTextContainingOrderByIdDesc(String keyword);
    // 【追加】名前（name）が完全に一致するものを探すメソッド
    // メソッドのルール：findBy + フィールド名（先頭大文字）
    List<Message> findByNameContainingOrderByIdDesc(String name);
    // ここには何も書かなくてOK！
    // JpaRepositoryを継承するだけで、保存(save)や全件取得(findAll)が使えるようになります。
}
