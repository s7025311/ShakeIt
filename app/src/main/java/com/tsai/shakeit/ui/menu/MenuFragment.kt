package com.tsai.shakeit.ui.menu

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.tsai.shakeit.databinding.MenuFragmentBinding
import com.tsai.shakeit.ext.getVmFactory
import com.tsai.shakeit.ui.detail.DrinksDetailFragmentDirections
import com.tsai.shakeit.ui.order.OrderFragmentDirections

class MenuFragment : Fragment() {

    private val viewModel by viewModels<MenuViewModel> {
        getVmFactory(
            shopData =
            MenuFragmentArgs.fromBundle(
                requireArguments()
            ).shopId,
            orderId = MenuFragmentArgs.fromBundle(
                requireArguments()
            ).orderId
        )
    }

    private lateinit var binding: MenuFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = MenuFragmentBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        val adapter = MenuAdapter(viewModel)

        viewModel.productList.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
        })

        viewModel.navToDetail.observe(viewLifecycleOwner, Observer {
            it?.let { findNavController().navigate(DrinksDetailFragmentDirections.navToDetail(it)) }
        })

        viewModel.popback.observe(viewLifecycleOwner, Observer {
            it?.let { findNavController().navigate(MenuFragmentDirections.navToHome()) }
        })

        viewModel.navToOrder.observe(viewLifecycleOwner, Observer {
            it?.let { findNavController().navigate(OrderFragmentDirections.navToOrder()) }
        })

        viewModel.shop.observe(viewLifecycleOwner, Observer {
            it?.let { binding.shopInfo = it }
        })

        viewModel.orderProduct.observe(viewLifecycleOwner, Observer { orderList ->
            if (!orderList.isNullOrEmpty()) {
                viewModel.hasOrder()
                viewModel.updateOrderTotalPrice(orderList.sumOf { it.price*it.qty })
            } else {
                viewModel.noOrder()
            }
            binding.textView9.text = orderList.size.toString()
        })

        viewModel.branchProduct.observe(viewLifecycleOwner, Observer {
           viewModel.filterMyList(it)
        })

        binding.recyclerView.adapter = adapter
        return binding.root
    }
}