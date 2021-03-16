restart_ipa:
  service.running:
      - name: pki-tomcatd@pki-tomcat
      - watch:
        - file: /var/lib/pki/pki-tomcat/conf/web.xml

replace_default_tomcat_error_page:
  file.replace:
    - name: /var/lib/pki/pki-tomcat/conf/web.xml
    - pattern: '</web-app>'
    - repl: "<error-page>
             <error-code>404</error-code>
                <location>/dummy.jsp</location>
             </error-page>
             <error-page>
             <error-code>403</error-code>
                <location>/dummy.jsp</location>
             </error-page>
             <error-page>
             <error-code>500</error-code>
                <location>/dummy.jsp</location>
             </error-page>
             </web-app>"
    - backup: False