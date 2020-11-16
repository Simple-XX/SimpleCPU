(setq path "/Users/cgk/ownCloud/课程/一生一芯/ict/riscv-tests/isa/")
(setq objcopy_1_1 "/Users/cgk/ownCloud/课程/一生一芯/ict/riscv64-unknown-elf-gcc-8.3.0-2020.04.0-x86_64-apple-darwin/bin/riscv64-unknown-elf-objcopy -O binary ")
(setq objcopy_1_2 " /Users/cgk/ownCloud/课程/一生一芯/ict/test.bin.ori")
(setq objcopy_2 "/Users/cgk/ownCloud/课程/一生一芯/ict/riscv64-unknown-elf-gcc-8.3.0-2020.04.0-x86_64-apple-darwin/bin/riscv64-unknown-elf-objcopy -I binary -O binary --reverse-bytes=4 /Users/cgk/ownCloud/课程/一生一芯/ict/test.bin.ori /Users/cgk/ownCloud/课程/一生一芯/ict/test.bin")
(setq sbt "sbt \"test:runMain Xim.SoC_Main_Type_Two --backend-name verilator\"")
(defun get_files(file_list)
    (setq tmp (readfile))
    ;(println tmp)
    ;(print "debug: file_list")
    ;(println file_list)
    (cond
        ((eq "." tmp) (get_files file_list)) ; do not need, skip
        ((eq ".." tmp) (get_files file_list)) ; do not need, skip
        ((eq nil tmp) file_list)
        (t (get_files (cons tmp file_list)))
    )
)

(defun filter(file_list ans)
    (setq tmp (car file_list))
    (cond
        ((eq tmp nil) ans)
        ((not (strin tmp "rv64ui-p-")) (filter (cdr file_list) ans))
        ((not (strin tmp "dump")) (filter (cdr file_list) (cons tmp ans))) ; if this is not a dump
        (t (filter (cdr file_list) ans))
    )
)

(defun append_path (file_list ans)
    (setq tmp (car file_list))
    (cond
        ((eq tmp nil) ans)
        (t (append_path (cdr file_list) (cons (stradd path tmp) ans)))
    )
)

(defun test (test_path_list)
    (setq tmp (car test_path_list))
    (cond
        ((eq tmp nil) 0)
        (t
            ; construct commands
            (println "************ New Test ************")
            (princ a )
            (setq a (+ a 1))
            (princ "th test case: ")
            (print tmp)
            (setq objcopy (stradd objcopy_1_1 tmp))
            (setq objcopy (stradd objcopy objcopy_1_2))
            (system objcopy)
            (setq objcopy objcopy_2)
            (system objcopy)
            (system sbt)
            (test (cdr test_path_list))
        )
    )
)

(setq a 1)
(diropen path)
(setq test_list (list))
(setq test_list (get_files test_list))
; (println test_list)
(setq ans (list))
(setq ans (filter test_list ans))
(setq tmp_list (list))
(setq ans (append_path ans tmp_list))
; (println ans)
(setq retcode (test ans))
(cond 
    ((eq retcode 0) (println "Test all done"))
    (t (println "Error in test"))
)
(exit 0)
