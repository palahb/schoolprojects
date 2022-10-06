; halil burak pala
; 2019400282
; compiling: yes
; complete: yes

#lang racket
(provide (all-defined-out))

;; given
(struct num (value grad)
    #:property prop:custom-write
    (lambda (num port write?)
        (fprintf port (if write? "(num ~s ~s)" "(num ~a ~a)")
            (num-value num) (num-grad num))))

(define get-value-of-num (lambda (num) (num-value num)))
(define get-value-of-list (lambda (list) (map second list)))
(define get-value (lambda (x) (if (num? x) (get-value-of-num x) (get-value-of-list x))))

(define get-grad-of-num (lambda (num) (num-grad num)))
(define get-grad-of-list (lambda (list) (map third list)))
(define get-grad (lambda (x) (if (num? x) (get-grad-of-num x) (get-grad-of-list x))))

(define get-grad-of-num-list (lambda (list) (map get-grad-of-num list)))

(define get-value-of-num-list (lambda (list) (map get-value-of-num list)))

(define convert (lambda (x) (eval x)))

(define add 
	(lambda args
		(let
			([va (eval (cons + (map get-value (map convert args))))]
			[gr (eval (cons + (map get-grad (map convert args))))])
			(eval (list num va gr))
		) 
	)
)

(define grad-mul
	(lambda (val-list grad-list)
		(if (and (= (length val-list) 2) (= (length grad-list) 2))
			(+ (* (first val-list) (second grad-list)) (* (second val-list) (first grad-list)))
			(+ 
				(* (car val-list) (grad-mul (cdr val-list) (cdr grad-list)))
				(eval (cons * (cons (car grad-list) (cdr val-list))))
			)
		)
	)
)

(define mul
	(lambda args
		(let
			([va (eval (cons * (map get-value (map convert args))))]
			[gr (grad-mul (get-value-of-num-list args) (get-grad-of-num-list args))])
			(eval (list num va gr))
		) 
	)
)

(define sub
	(lambda args
		(let
			([va (eval (cons - (map get-value (map convert args))))]
			[gr (eval (cons - (map get-grad (map convert args))))])
			(eval (list num va gr))
		) 
	)
)

;; given
(define relu (lambda (x) (if (> (num-value x) 0) x (num 0.0 0.0))))
;; given
(define mse (lambda (x y) (mul (sub x y) (sub x y))))

(define make-hash-list
	(lambda (names numbers)
		(if (= (length names) 0)
			'()
			(append (list (cons (first names) (first numbers))) (make-hash-list (cdr names) (cdr numbers)))
		)
	)
)

(define get-nums-for-hashing
	(lambda (names values var)
		(if (= (length names) 0)
			'()
			(append 
				(if (eq? (first names) var)
					(list (eval (list num (first values) 1.0)))
					(list (eval (list num (first values) 0.0)))
				)
				(get-nums-for-hashing (cdr names) (cdr values) var)
			)
		)
	)
)

(define create-hash
	(lambda (names values var)
		(make-hash (make-hash-list names (get-nums-for-hashing names values var)))
	)
)

(define parse
	(lambda (hash expr)
		(cond
			[(= (length expr) 0) '()]
			[(= (length expr) 1)
				(cond
					[(eq? (first expr) '+) (list 'add)]
					[(eq? (first expr) '-) (list 'sub)]
					[(eq? (first expr) '*) (list 'mul)]
					[(eq? (first expr) 'mse) (list 'mse)]
					[(eq? (first expr) 'relu) (list 'relu)]
					[(number? (first expr)) (list (num (first expr) 0.0))]
					[(list? (first expr)) (list (parse hash (first expr)))]
					[else (list (hash-ref hash (first expr)))]
				)
			]
			[else (append (parse hash (list (first expr))) (parse hash (cdr expr)))]
		)
	)
)

(define grad
	(lambda (names values var expr)
		(get-grad (eval (parse (create-hash names values var) expr)))
	)
)

(define partial-grad
	(lambda (names values vars expr)
		(map (lambda (var) (if (member var vars) 
			(grad names values var expr)
			(grad names values 0 expr)
			)) names)
	)
)

(define mul-list
	(lambda (x lst)
		(if (null? lst)
      	'()
      	(cons (* x (car lst)) (mul-list x (cdr lst)))
		)
	)
)

(define gradient-descent
	(lambda (names values vars lr expr)
		(map - values (mul-list lr (partial-grad names values vars expr )))
	)
)

(define optimize
	(lambda (names values vars lr k expr)
		(if (= k 1)
			(gradient-descent names values vars lr expr)
			(optimize names (gradient-descent names values vars lr expr) vars lr (- k 1) expr)
		)
	)
)