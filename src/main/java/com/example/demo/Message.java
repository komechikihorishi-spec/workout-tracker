package com.example.demo;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.hibernate.annotations.CreationTimestamp;

@Entity // 「これはDBのテーブルだよ」という目印
public class Message {
	@CreationTimestamp// 保存した時の時間を自動で入れる魔法の目印
	private LocalDateTime createdAt;
	
    @Id // 「これがID（主キー）だよ」という目印
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 自動で1, 2, 3...と番号を振る
    private Long id;

    public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	@NotBlank(message = "メッセージを入力してください") // 空っぽ禁止
    @Size(max = 20, message = "20文字以内で入力してください") // 文字数制限
    private String text; // 投稿メッセージを入れる変数
    
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
