CREATE OR REPLACE FUNCTION set_employee_name(p_employee_id IN NUMBER, p_first_name IN VARCHAR2, p_last_name IN VARCHAR2)
  RETURN VARCHAR2 IS
  l_old_first_name employees.first_name%TYPE;
  l_old_last_name  employees.last_name%TYPE;
  BEGIN
    SELECT
      first_name,
      last_name
    INTO l_old_first_name, l_old_last_name
    FROM employees
    WHERE employee_id = p_employee_id;

    UPDATE employees
    SET first_name = p_first_name, last_name = p_last_name
    WHERE employee_id = p_employee_id;

    RETURN l_old_first_name || ' ' || l_old_last_name;
  END;
///

CREATE OR REPLACE PROCEDURE set_employee_name_out(p_employee_id IN NUMBER, p_first_name IN VARCHAR2,
                                                  p_last_name   IN VARCHAR2, p_old_name OUT VARCHAR2)
IS
  l_old_first_name employees.first_name%TYPE;
  l_old_last_name  employees.last_name%TYPE;
  BEGIN
    SELECT
      first_name,
      last_name
    INTO l_old_first_name, l_old_last_name
    FROM employees
    WHERE employee_id = p_employee_id;

    UPDATE employees
    SET first_name = p_first_name, last_name = p_last_name
    WHERE employee_id = p_employee_id;

    p_old_name := l_old_first_name || ' ' || l_old_last_name;
  END;
///

CREATE OR REPLACE FUNCTION get_employee_name(p_employee_id IN NUMBER)
  RETURN VARCHAR2 IS
  l_first_name employees.first_name%TYPE;
  l_last_name  employees.last_name%TYPE;
  BEGIN
    SELECT
      first_name,
      last_name
    INTO l_first_name, l_last_name
    FROM employees
    WHERE employee_id = p_employee_id;

    RETURN l_first_name || ' ' || l_last_name;
  END;
///

CREATE OR REPLACE FUNCTION get_employees(p_department_id IN NUMBER)
  RETURN SYS_REFCURSOR IS
  v_rc SYS_REFCURSOR;
  BEGIN
    -- Open some "secret" select cursor
    OPEN v_rc FOR
    SELECT *
    FROM employees
    WHERE department_id = p_department_id;

    RETURN v_rc;
  END;
///

CREATE OR REPLACE PROCEDURE get_employees_out(p_department_id IN NUMBER, p_result_set OUT SYS_REFCURSOR)
IS
  BEGIN
    -- Open some "secret" select cursor
    OPEN p_result_set FOR
    SELECT *
    FROM employees
    WHERE department_id = p_department_id;
  END;
///

CREATE OR REPLACE TYPE employee_structtype FORCE AS OBJECT (
  employee_id NUMBER(6),
  first_name  VARCHAR2(20),
  last_name   VARCHAR2(25)
);


///

CREATE OR REPLACE TYPE employee_arraytype FORCE AS TABLE OF employee_structtype;


///

CREATE OR REPLACE PROCEDURE insert_employees(p_employees IN  employee_arraytype,
                                             p_department_id employees.department_id%TYPE)
IS
  BEGIN
    FORALL indx IN 1 .. p_employees.count
    INSERT INTO employees (employee_id, first_name, last_name, email, hire_date, job_id, department_id) VALUES
      (employees_seq.nextval, p_employees(indx).first_name, p_employees(indx).last_name,
       employees_seq.currval || '.email@dot.com', SYSDATE, -- Email has unique constraint :-)
       'IT_PROG', p_department_id);
  END;

///

CREATE OR REPLACE FUNCTION get_employees_array(p_department_id employees.department_id%TYPE)
  RETURN employee_arraytype IS
  l_employees employee_arraytype;
  BEGIN
    SELECT employee_structtype(employee_id, first_name, last_name)
    BULK COLLECT INTO l_employees
    FROM employees
    WHERE department_id = p_department_id;
    RETURN l_employees;
  END;

///

CREATE OR REPLACE PROCEDURE get_employees_array_out(p_department_id employees.department_id%TYPE,
                                                    p_employees OUT employee_arraytype)
IS
  BEGIN
    SELECT employee_structtype(employee_id, first_name, last_name)
    BULK COLLECT INTO p_employees
    FROM employees
    WHERE department_id = p_department_id;
  END;

///

CREATE OR REPLACE FUNCTION pipe_employees_cursor(p_employees_cursor SYS_REFCURSOR)
  RETURN employee_arraytype PIPELINED IS
  l_employee_record employees%ROWTYPE;
  BEGIN
    LOOP
      FETCH p_employees_cursor INTO l_employee_record;
      EXIT WHEN p_employees_cursor%NOTFOUND;

      PIPE ROW (employee_structtype(l_employee_record.employee_id, l_employee_record.first_name,
                                    l_employee_record.last_name));
    END LOOP;

    RETURN;
  END;

///