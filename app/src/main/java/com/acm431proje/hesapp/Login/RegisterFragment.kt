package com.acm431proje.hesapp.Login

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.acm431proje.hesapp.databinding.FragmentRegisterBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore


class RegisterFragment : Fragment() {

    private lateinit var binding : FragmentRegisterBinding

    private lateinit var auth : FirebaseAuth
    private lateinit var firestore : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = Firebase.auth
        firestore = Firebase.firestore
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnRegister.setOnClickListener {
            hideKeyboard()
            register()
        }
    }


    private fun register(){
        val name = binding.textRegisterName.text.toString()
        val surname = binding.textRegisterSurname.text.toString()
        val email = binding.textRegisterEmail.text.toString()
        val password = binding.textRegisterPassword.text.toString()
        val password2 = binding.textRegisterPassword2.text.toString()


        if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            binding.textInputError.text = "Lütfen tüm alanları doldurunuz!"
        } else {

            if (!isValidEmail(email)){
                binding.textInputError.text = "Mail adresiniz geçersiz,lütfen geçerli bir adres değil."
                return
            } else if (password != password2){
                binding.textInputError.text = "Şifreleriniz uyuşmuyor. Lütfen gözden geçiriniz."
                return
            } else {

                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Kayıt başarılıysa, kullanıcı detayları alınır.
                        val firebaseUser: FirebaseUser? = task.result?.user
                        val uid = firebaseUser?.uid

                        if (!uid.isNullOrEmpty()) {

                            // Kullanıcının bilgiler, 'users' koleksiyonuna kaydedilir.
                            val user = hashMapOf("name" to name, "surname" to surname, "email" to email, "uid" to uid)

                            firestore.collection("users").document(email)
                                .set(user).addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Başarıyla kaydoldunuz!", Toast.LENGTH_LONG).show()
                                    //findNavController().navigate(action_registerFragment_to_loginFragment)
                                }.addOnFailureListener {
                                    Toast.makeText(requireContext(),"Kaydolurken bir hata meydana geldi!", Toast.LENGTH_LONG).show()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Kullanıcı kaydı başarısız oldu: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }.addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Kullanıcı kaydı başarısız oldu: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
    }
}